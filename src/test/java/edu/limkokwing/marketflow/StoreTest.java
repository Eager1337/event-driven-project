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
    @Test void concurrentSalesCannotOversell()throws Exception{
        Product product=p();ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> buy=()->{start.await();try{store.checkout(List.of(new Line(product,4)),0,Payment.CARD,BigDecimal.ZERO);return true;}catch(IllegalArgumentException e){return false;}};
        try{Future<Boolean> a=pool.submit(buy),b=pool.submit(buy);start.countDown();assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));assertEquals(1,p().stock());assertEquals(1,store.sales().size());}finally{pool.shutdownNow();}
    }
}