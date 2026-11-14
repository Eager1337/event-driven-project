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
}