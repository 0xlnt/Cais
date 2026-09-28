package com.projeto.caisapp;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Local demonstration data; no network or payment processing. */
public final class LocalStore {
    private final SharedPreferences prefs;
    public final Map<String,Integer> cart=new LinkedHashMap<>();
    public final Set<String> favorites=new HashSet<>();
    public JSONArray orders;
    public LocalStore(Context context) {
        prefs=context.getSharedPreferences("cais_local_v1",Context.MODE_PRIVATE);
        favorites.addAll(prefs.getStringSet("favorites",new HashSet<>()));
        orders=parseArray(prefs.getString("orders","[]"));
        try {
            JSONObject saved=new JSONObject(prefs.getString("cart","{}"));
            for(Catalog.Product p:Catalog.PRODUCTS) {
                int n=saved.optInt(p.id,0);
                if(n>0 && n<=40) cart.put(p.id,n);
            }
        } catch(JSONException ignored) { }
    }
    private JSONArray parseArray(String value) {
        try { return new JSONArray(value); } catch(JSONException e) { return new JSONArray(); }
    }
    public String get(String key,String fallback) { return prefs.getString(key,fallback); }
    public void put(String key,String value) { prefs.edit().putString(key,value).apply(); }
    public void save() {
        prefs.edit().putString("cart",new JSONObject(cart).toString())
            .putStringSet("favorites",new HashSet<>(favorites)).putString("orders",orders.toString()).apply();
    }
    public void toggleFavorite(String id) {
        if(!favorites.add(id)) favorites.remove(id);
        save();
    }
    public void add(String id,int units) {
        cart.put(id,Math.min(40,cart.getOrDefault(id,0)+units)); save();
    }
    public long subtotal() {
        long total=0;
        for(Map.Entry<String,Integer> e:cart.entrySet()) {
            Catalog.Product p=Catalog.find(e.getKey());
            if(p!=null) total+=Catalog.lineTotal(p.cents,e.getValue());
        }
        return total;
    }
    public JSONObject placeOrder(boolean delivery,String payment,String address,String note) throws JSONException {
        if(cart.isEmpty()) throw new IllegalStateException("Carrinho vazio");
        JSONArray lines=new JSONArray();
        for(Map.Entry<String,Integer> e:cart.entrySet()) {
            Catalog.Product p=Catalog.find(e.getKey());
            lines.put(new JSONObject().put("product",p.id).put("units",e.getValue()).put("cents",p.cents));
        }
        long now=System.currentTimeMillis();
        JSONObject order=new JSONObject().put("id","CAI-"+now).put("created",now)
            .put("items",lines).put("subtotal",subtotal()).put("fee",delivery?800:0)
            .put("total",subtotal()+(delivery?800:0)).put("delivery",delivery)
            .put("payment",payment).put("address",address).put("note",note).put("status","Em preparação");
        JSONArray next=new JSONArray();next.put(order);
        for(int i=0;i<orders.length();i++) next.put(orders.get(i));
        orders=next;cart.clear();save();return order;
    }
    public JSONArray messages(int seller) { return parseArray(get("chat_"+seller,"[]")); }
    public void send(int seller,String message) throws JSONException {
        JSONArray messages=messages(seller);
        messages.put(new JSONObject().put("mine",true).put("text",message));
        messages.put(new JSONObject().put("mine",false).put("text",
            "Resposta de demonstração: recebemos sua mensagem! A limpeza e o corte podem ser combinados na retirada."));
        put("chat_"+seller,messages.toString());
    }
}
