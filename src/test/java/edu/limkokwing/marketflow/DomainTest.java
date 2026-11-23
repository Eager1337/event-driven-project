package edu.limkokwing.marketflow;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static edu.limkokwing.marketflow.Domain.*;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {
    private Product product(String price,int stock){return new Product(1,"sku","Test item","General",new BigDecimal(price),stock,5,true);}
    @Test void validatesRequiredFields(){assertThrows(IllegalArgumentException.class,()->new Product(0," ","Item","General",BigDecimal.ONE,1,1,true));}
    @Test void normalizesSku(){assertEquals("SKU",product("1.00",1).sku());}
    @Test void rejectsNegativePrice(){assertThrows(IllegalArgumentException.class,()->product("-1",1));}
    @Test void rejectsNegativeStock(){assertThrows(IllegalArgumentException.class,()->product("1",-1));}
    @Test void exactDecimalArithmetic(){Cart c=new Cart();c.add(product("0.10",5),3);assertEquals(new BigDecimal("0.30"),c.total(0));}
    @Test void mergesSameProduct(){Cart c=new Cart();Product p=product("10",5);c.add(p,1);c.add(p,2);assertEquals(1,c.lines().size());assertEquals(3,c.lines().get(0).quantity());}
    @Test void rejectsExcessQuantity(){Cart c=new Cart();Product p=product("10",2);c.add(p,1);assertThrows(IllegalArgumentException.class,()->c.add(p,2));assertEquals(1,c.lines().get(0).quantity());}
    @Test void rejectsZeroQuantity(){assertThrows(IllegalArgumentException.class,()->new Cart().add(product("10",2),0));}
    @Test void appliesDiscountWithRounding(){Cart c=new Cart();c.add(product("12.35",2),1);assertEquals(new BigDecimal("11.12"),c.total(10));}
    @Test void rejectsInvalidDiscount(){assertThrows(IllegalArgumentException.class,()->new Cart().total(51));}
    @Test void clearsAndRemoves(){Cart c=new Cart();c.add(product("10",2),1);c.remove(1);assertTrue(c.lines().isEmpty());c.add(product("10",2),1);c.clear();assertTrue(c.lines().isEmpty());}
    @Test void snapshotIsImmutable(){Cart c=new Cart();c.add(product("10",2),1);assertThrows(UnsupportedOperationException.class,()->c.lines().clear());}
    @Test void archivedProductCannotBeSold(){assertThrows(IllegalArgumentException.class,()->new Line(new Product(1,"X","Item","General",BigDecimal.ONE,5,1,false),1));}
    @Test void polymorphicTableModels(){Tables.TypedModel<Product> model=new Tables.Products();model.setRows(java.util.List.of(product("10",2)));assertEquals("Low stock",model.getValueAt(0,6));assertFalse(model.isCellEditable(0,0));}
    @Test void numericCartSortingUsesNumbersNotStrings(){
        Tables.CartLines model=new Tables.CartLines();
        model.setRows(java.util.List.of(new Line(product("10",20),10),new Line(product("10",20),2)));
        javax.swing.table.TableRowSorter<Tables.CartLines> sorter=new javax.swing.table.TableRowSorter<>(model);
        sorter.toggleSortOrder(1);assertEquals(2,model.getValueAt(sorter.convertRowIndexToModel(0),1));
        assertEquals(java.math.BigDecimal.class,model.getColumnClass(3));
    }
    @Test void numericReceiptSortingAndModelNotification(){
        Tables.Sales model=new Tables.Sales();java.util.concurrent.atomic.AtomicInteger events=new java.util.concurrent.atomic.AtomicInteger();model.addTableModelListener(e->events.incrementAndGet());
        java.time.LocalDateTime time=java.time.LocalDateTime.of(2026,11,23,10,0);
        model.setRows(java.util.List.of(new Sale(10,time,BigDecimal.TEN,0,BigDecimal.TEN,Payment.CARD,BigDecimal.TEN,BigDecimal.ZERO),new Sale(2,time,BigDecimal.ONE,0,BigDecimal.ONE,Payment.CASH,BigDecimal.ONE,BigDecimal.ZERO)));
        javax.swing.table.TableRowSorter<Tables.Sales> sorter=new javax.swing.table.TableRowSorter<>(model);sorter.toggleSortOrder(0);
        assertEquals(2L,model.getValueAt(sorter.convertRowIndexToModel(0),0));assertEquals(1,events.get());assertEquals(java.time.LocalDateTime.class,model.getColumnClass(1));
    }
}