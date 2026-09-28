package com.projeto.caisapp;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Catalog {
    public static final Locale BR = new Locale("pt", "BR");
    public static final class Seller {
        public final String name, shop, rating;
        public final int image, featuredImage;
        Seller(String name, String shop, String rating, int image, int featuredImage) {
            this.name=name; this.shop=shop; this.rating=rating; this.image=image; this.featuredImage=featuredImage;
        }
    }
    public static final List<Seller> SELLERS = Arrays.asList(
        new Seller("Noah Ludwig", "Revendedor do Cais", "4,97", R.drawable.seller_noah,R.drawable.seller_noah),
        new Seller("Lúcia Barreto", "Peixaria Ribeirinha", "4,98", R.drawable.seller_lucia,R.drawable.seller_lucia),
        new Seller("Carlos Braga", "Amazonas Pescados", "4,92", R.drawable.seller_carlos,R.drawable.seller_carlos_featured),
        new Seller("Sofia Costa", "Tião do peixe", "4,74", R.drawable.seller_sofia,R.drawable.seller_sofia));
    public static final class Product {
        public final String id, name, category, origin, rating;
        public final int cents, image, homeImage, seller;
        public final boolean promotion;
        Product(String id,String name,String category,String origin,String rating,int cents,
                int image,int homeImage,int seller,boolean promotion) {
            this.id=id;this.name=name;this.category=category;this.origin=origin;this.rating=rating;
            this.cents=cents;this.image=image;this.homeImage=homeImage;this.seller=seller;this.promotion=promotion;
        }
        public Seller seller() { return SELLERS.get(seller); }
    }
    public static final List<Product> PRODUCTS=Arrays.asList(
        new Product("tambaqui","Tambaqui","Peixes","Peixe de rio","4,90",1821,R.drawable.fish_tambaqui,R.drawable.fish_tambaqui_home,0,true),
        new Product("matrinxa","Matrinxã","Peixes","Peixe de cativeiro","5,00",3225,R.drawable.fish_matrinxa,R.drawable.fish_matrinxa,1,false),
        new Product("pirarucu","Pirarucu","Peixes","Peixe de cativeiro","4,89",6135,R.drawable.fish_pirarucu,R.drawable.fish_pirarucu,2,false),
        new Product("tambaqui_carlos","Tambaqui","Peixes","Peixe de rio","5,00",3890,R.drawable.fish_tambaqui,R.drawable.fish_tambaqui_home,2,false),
        new Product("camarao","Camarão","Camarão","Frutos do mar","4,95",5490,R.drawable.fish_camarao,R.drawable.fish_camarao,3,true),
        new Product("polvo","Polvo","Frutos do Mar","Frutos do mar","4,88",6990,R.drawable.fish_polvo,R.drawable.fish_polvo,2,false),
        new Product("jaraqui","Jaraqui","Peixes","Peixe de rio","4,86",1590,R.drawable.fish_jaraqui,R.drawable.fish_jaraqui,1,true));
    public static Product find(String id) {
        for(Product p:PRODUCTS) if(p.id.equals(id)) return p;
        return null;
    }
    public static String normalize(String s) {
        return Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(BR).trim();
    }
    public static boolean matches(Product p,String query,String category,boolean favoritesOnly,boolean favorite) {
        String haystack=p.name+" "+p.origin+" "+p.seller().name+" "+p.seller().shop;
        boolean cat=category.equals("Todos") || category.equals("Promoções") && p.promotion
            || category.equals("Frutos do Mar") && !p.category.equals("Peixes") || p.category.equals(category);
        return cat && normalize(haystack).contains(normalize(query)) && (!favoritesOnly || favorite);
    }
    public static long lineTotal(int cents,int halfKilos) {
        if(cents<0 || halfKilos<1 || halfKilos>40) throw new IllegalArgumentException("Quantidade inválida");
        return (cents*(long)halfKilos+1)/2;
    }
    public static String money(long cents) { return NumberFormat.getCurrencyInstance(BR).format(cents/100.0); }
    public static String kilos(int units) { return String.format(BR,"%.1f kg",units/2.0); }
    private Catalog() {}
}
