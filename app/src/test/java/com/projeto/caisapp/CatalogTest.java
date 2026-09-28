package com.projeto.caisapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class CatalogTest {
    @Test public void roundsHalfKiloToNearestCent() {
        assertEquals(911L,Catalog.lineTotal(1821,1));
        assertEquals(1821L,Catalog.lineTotal(1821,2));
        assertEquals(4838L,Catalog.lineTotal(3225,3));
        assertEquals(36420L,Catalog.lineTotal(1821,40));
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsZeroQuantity() {
        Catalog.lineTotal(1821,0);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsQuantityAboveTwentyKilos() {
        Catalog.lineTotal(1821,41);
    }
    @Test public void searchIgnoresAccentsAndFindsSeller() {
        Catalog.Product fish=Catalog.find("matrinxa");
        assertTrue(Catalog.matches(fish,"  MATRINXA ","Todos",false,false));
        assertTrue(Catalog.matches(fish,"lucia","Peixes",false,false));
        assertFalse(Catalog.matches(fish,"lucia","Frutos do Mar",false,false));
        assertFalse(Catalog.matches(fish,"","Todos",true,false));
        assertTrue(Catalog.matches(fish,"","Todos",true,true));
    }
    @Test public void promotionsAndSeafoodAreFiltered() {
        assertTrue(Catalog.matches(Catalog.find("camarao"),"camarao","Frutos do Mar",false,false));
        assertTrue(Catalog.matches(Catalog.find("tambaqui"),"","Promoções",false,false));
        assertFalse(Catalog.matches(Catalog.find("pirarucu"),"","Promoções",false,false));
    }
}
