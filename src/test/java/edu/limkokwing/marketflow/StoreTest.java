package edu.limkokwing.marketflow;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import static edu.limkokwing.marketflow.Domain.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class StoreTest {
    static Database testDatabase() {
        String url=System.getenv("TEST_DB_URL");
        if(url==null || !url.contains("/marketflow_test?"))throw new IllegalStateException("Tests require an isolated marketflow_test database.");
        return new Database(url,System.getenv("TEST_DB_USER"),System.getenv("TEST_DB_PASSWORD"));
    }
    Database db; Store store;
    @BeforeEach void reset()throws Exception{
        db=testDatabase();store=new Store(db);
        try(Connection c=db.open();Statement s=c.createStatement()){s.executeUpdate("DELETE FROM sale_items");s.executeUpdate("DELETE FROM sales");s.executeUpdate("DELETE FROM products");}
        store.save(new Product(0,"TEST-1","Test product","Test",new BigDecimal("10.00"),5,1,true));
    }
    Product p()throws Exception{return store.products(false).get(0);}
    @Test void savesAndReadsProducts()throws Exception{assertEquals("TEST-1",p().sku());assertEquals(5,p().stock());}
    @Test void cashCheckoutPersistsAndReducesStock()throws Exception{
        Receipt r=store.checkout(List.of(new Line(p(),2)),10,Payment.CASH,new BigDecimal("20"));
        assertEquals(new BigDecimal("18.00"),r.sale().total());assertEquals(new BigDecimal("2.00"),r.sale().change());assertEquals(3,p().stock());assertEquals(1,store.sales().size());
        assertEquals(r.text(),store.receipt(r.sale().id()).text());
    }
    @Test void insufficientCashRollsBack()throws Exception{assertThrows(IllegalArgumentException.class,()->store.checkout(List.of(new Line(p(),2)),0,Payment.CASH,BigDecimal.ONE));assertEquals(5,p().stock());assertTrue(store.sales().isEmpty());}
    @Test void cardTenderEqualsTotal()throws Exception{Receipt r=store.checkout(List.of(new Line(p(),1)),0,Payment.CARD,BigDecimal.ZERO);assertEquals(new BigDecimal("10.00"),r.sale().tendered());assertEquals(new BigDecimal("0.00"),r.sale().change());}
    @Test void stalePriceRejected()throws Exception{Product old=p();store.save(new Product(old.id(),old.sku(),old.name(),old.category(),new BigDecimal("15"),old.stock(),1,true),old);assertThrows(IllegalArgumentException.class,()->store.checkout(List.of(new Line(old,1)),0,Payment.CARD,BigDecimal.ZERO));assertTrue(store.sales().isEmpty());}
    @Test void archiveKeepsReceiptSnapshots()throws Exception{Product old=p();Receipt r=store.checkout(List.of(new Line(old,1)),0,Payment.CARD,BigDecimal.ZERO);store.archive(old.id());assertTrue(store.products(false).isEmpty());assertEquals(old.name(),store.receipt(r.sale().id()).lines().get(0).product().name());}
    @Test void optimisticEditProtectsCheckoutStock()throws Exception{Product old=p();store.checkout(List.of(new Line(old,1)),0,Payment.CARD,BigDecimal.ZERO);assertThrows(SQLException.class,()->store.save(new Product(old.id(),old.sku(),"Updated",old.category(),old.price(),old.stock(),1,true),old));assertEquals(4,p().stock());}
    @Test void emptyCartRejected(){assertThrows(IllegalArgumentException.class,()->store.checkout(List.of(),0,Payment.CARD,BigDecimal.ZERO));}
    @Test void duplicateSkuRejected()throws Exception{assertThrows(SQLIntegrityConstraintViolationException.class,()->store.save(new Product(0,"TEST-1","Duplicate","Test",BigDecimal.ONE,1,1,true)));}
    @Test void keysetPagesHaveNoDuplicatesAndIgnoreNewerInserts()throws Exception{
        Product product=p();
        for(int i=0;i<5;i++)store.checkout(List.of(new Line(product,1)),0,Payment.CARD,BigDecimal.ZERO);
        Store.SalesPage first=store.salesPage(0,2),second=store.salesPage(first.olderCursor(),2),last=store.salesPage(second.olderCursor(),2);
        assertTrue(first.hasOlder());assertTrue(second.hasOlder());assertFalse(last.hasOlder());assertEquals(1,last.rows().size());
        java.util.Set<Long> ids=new java.util.HashSet<>();
        for(Store.SalesPage page:List.of(first,second,last))for(Sale sale:page.rows())assertTrue(ids.add(sale.id()));
        assertEquals(5,ids.size());assertTrue(first.rows().get(1).id()>second.rows().get(0).id());
        store.save(new Product(product.id(),product.sku(),product.name(),product.category(),product.price(),2,1,true));
        store.checkout(List.of(new Line(p(),1)),0,Payment.CARD,BigDecimal.ZERO);
        assertEquals(second.rows(),store.salesPage(first.olderCursor(),2).rows());
        assertTrue(store.salesPage(0,2).rows().get(0).id()>first.rows().get(0).id());
        assertThrows(UnsupportedOperationException.class,()->first.rows().clear());
    }
    @Test void emptyPageAndInvalidPageParameters()throws Exception{
        assertTrue(store.salesPage(0,50).rows().isEmpty());assertFalse(store.salesPage(0,50).hasOlder());
        assertThrows(IllegalArgumentException.class,()->store.salesPage(-1,50));
        assertThrows(IllegalArgumentException.class,()->store.salesPage(0,0));
        assertThrows(IllegalArgumentException.class,()->store.salesPage(0,1001));
    }
    @Test void syntheticQueryPlanUsesPrimaryIndex()throws Exception{
        // Only the isolated test database; no real user sales are used or changed.
        try(Connection c=db.open();PreparedStatement s=c.prepareStatement("INSERT INTO sales(subtotal,discount_percent,total,payment,tendered,change_due) VALUES(10,0,10,'CARD',10,0)")){
            c.setAutoCommit(false);for(int i=0;i<5000;i++){s.addBatch();if(i%500==499)s.executeBatch();}c.commit();
        }
        long cursor;
        try(Connection c=db.open();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id FROM sales ORDER BY id DESC LIMIT 1 OFFSET 2500")){assertTrue(r.next());cursor=r.getLong(1);}
        String query="SELECT * FROM sales WHERE id < "+cursor+" ORDER BY id DESC LIMIT 51";
        String key,type,plan;
        try(Connection c=db.open();Statement s=c.createStatement();ResultSet r=s.executeQuery("EXPLAIN "+query)){assertTrue(r.next());key=r.getString("key");type=r.getString("type");assertEquals("PRIMARY",key);assertEquals("range",type);}
        try(Connection c=db.open();Statement s=c.createStatement();ResultSet r=s.executeQuery("EXPLAIN ANALYZE "+query)){assertTrue(r.next());plan=r.getString(1);}
        assertTrue(plan.contains("Limit: 51"));assertEquals(50,store.salesPage(cursor,50).rows().size());
        String version;try(Connection c=db.open()){version=c.getMetaData().getDatabaseProductVersion();}
        java.nio.file.Files.writeString(java.nio.file.Path.of("docs/PERFORMANCE-EVIDENCE.md"),
            "# Isolated sales-history query evidence\n\nGenerated by `StoreTest.syntheticQueryPlanUsesPrimaryIndex` on MySQL "+version+".\n\n"+
            "Dataset: 5,000 synthetic sale headers in `marketflow_test`; no business database records. Query is a deep-history page; its cursor identifies the midpoint.\n\n"+
            "```sql\n"+query+"\n```\n\nEXPLAIN: key `"+key+"`, access type `"+type+"`.\n\n```text\n"+plan+"\n```\n\n"+
            "The page requests at most 51 rows: 50 displayed plus one look-ahead. Primary-key range access avoids scanning an increasingly deep OFFSET prefix.\n\n"+
            "This is one local synthetic query-plan measurement, not a production capacity benchmark. Server timings vary. It does not establish inventory scaling, connection-pool throughput, end-to-end latency, or concurrent-user capacity. The OFFSET above is only used to choose a test cursor, not by the application.\n");
    }
    @Test void concurrentSalesCannotOversell()throws Exception{
        Product product=p();ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> buy=()->{start.await();try{store.checkout(List.of(new Line(product,4)),0,Payment.CARD,BigDecimal.ZERO);return true;}catch(IllegalArgumentException e){return false;}};
        try{Future<Boolean> a=pool.submit(buy),b=pool.submit(buy);start.countDown();assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));assertEquals(1,p().stock());assertEquals(1,store.sales().size());}finally{pool.shutdownNow();}
    }
}