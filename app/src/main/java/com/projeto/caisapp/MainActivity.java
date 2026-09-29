package com.projeto.caisapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

/** Native Android views in Java, with responsive layouts and a persistent local demo. */
public class MainActivity extends AppCompatActivity {
    private static final int NAVY=Color.rgb(8,51,89), CYAN=Color.rgb(45,203,220),
        PALE=Color.rgb(225,234,242), MUTED=Color.rgb(104,129,148), BORDER=Color.rgb(202,210,218);
    private FrameLayout container;
    private LinearLayout navigation, body, results;
    private LocalStore store;
    private String screen="home", productId="tambaqui", query="", category="Todos", orderId="", orderFilter="Todos";
    private String payment="PIX", addressDraft="", noteDraft="";
    private boolean favoritesOnly=false, delivery=false;
    private int units=2, sellerIndex=0, sort=0, bannerIndex=0;
    private final Deque<String> history=new ArrayDeque<>();
    private static final int[] RECOMMENDED_PRODUCTS={4,5,6,1,2,0};
    private final Handler recommendationHandler=new Handler(Looper.getMainLooper());
    private LinearLayout recommendationItems;
    private int recommendationOffset=0;
    private boolean resumed=false;
    private final Runnable rotateRecommendations=new Runnable() {
        @Override public void run() {
            if(!resumed || !screen.equals("home") || recommendationItems==null)return;
            final LinearLayout items=recommendationItems;
            items.animate().alpha(0f).translationY(dp(4)).setDuration(160).withEndAction(()->{
                if(!resumed || !screen.equals("home") || recommendationItems!=items)return;
                recommendationOffset=(recommendationOffset+1)%RECOMMENDED_PRODUCTS.length;
                populateProductCards(items,true);
                items.animate().alpha(1f).translationY(0f).setDuration(220).withEndAction(null).start();
                scheduleRecommendations();
            }).start();
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store=new LocalStore(this);
        addressDraft=store.get("address","");
        if(state!=null) {
            screen=state.getString("screen","home");productId=state.getString("product","tambaqui");
            query=state.getString("query","");category=state.getString("category","Todos");
            favoritesOnly=state.getBoolean("favorites");units=state.getInt("units",2);
            sellerIndex=state.getInt("seller",0);sort=state.getInt("sort",0);
            delivery=state.getBoolean("delivery");payment=state.getString("payment","PIX");
            addressDraft=state.getString("address",addressDraft);noteDraft=state.getString("note","");
            orderId=state.getString("order","");orderFilter=state.getString("orderFilter","Todos");
            recommendationOffset=state.getInt("recommendationOffset",0);
            ArrayList<String> stack=state.getStringArrayList("history");if(stack!=null) history.addAll(stack);
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        container=findViewById(R.id.screen_container);navigation=findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return insets;
        });
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if(!history.isEmpty()) {
                    String[] route=history.pop().split("\\|");
                    screen=route[0];
                    if(route.length==4){productId=route[1];units=Integer.parseInt(route[2]);sellerIndex=Integer.parseInt(route[3]);}
                    render();
                }
                else if(!screen.equals("home")) tab("home");
                else {setEnabled(false);getOnBackPressedDispatcher().onBackPressed();}
            }
        });
        render();
    }
    @Override protected void onResume() {
        super.onResume();resumed=true;scheduleRecommendations();
    }
    @Override protected void onPause() {
        resumed=false;stopRecommendations();super.onPause();
    }
    private void stopRecommendations() {
        recommendationHandler.removeCallbacks(rotateRecommendations);
        if(recommendationItems!=null) {
            recommendationItems.animate().cancel();
            recommendationItems.setAlpha(1f);recommendationItems.setTranslationY(0f);
        }
    }
    private void scheduleRecommendations() {
        recommendationHandler.removeCallbacks(rotateRecommendations);
        if(resumed && screen.equals("home") && recommendationItems!=null)
            recommendationHandler.postDelayed(rotateRecommendations,6000);
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("screen",screen);out.putString("product",productId);out.putString("query",query);
        out.putString("category",category);out.putBoolean("favorites",favoritesOnly);out.putInt("units",units);
        out.putInt("seller",sellerIndex);out.putInt("sort",sort);out.putBoolean("delivery",delivery);
        out.putString("payment",payment);out.putString("address",addressDraft);out.putString("note",noteDraft);
        out.putString("order",orderId);out.putString("orderFilter",orderFilter);
        out.putStringArrayList("history",new ArrayList<>(history));
        out.putInt("recommendationOffset",recommendationOffset);
    }
    private int dp(float n) {return Math.round(n*getResources().getDisplayMetrics().density);}
    private LinearLayout column() {LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row() {LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout.LayoutParams lp(int w,int h) {return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private void pad(View v,int n) {v.setPadding(dp(n),dp(n),dp(n),dp(n));}
    private GradientDrawable background(int color,int radius,boolean border) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));
        if(border) d.setStroke(dp(1),BORDER);return d;
    }
    private TextView text(String value,int size,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextColor(NAVY);t.setTextSize(size);
        t.setFontFeatureSettings("kern");if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private void label(LinearLayout parent,String value,int size,boolean bold) {
        parent.addView(text(value,size,bold),lp(-1,-2));
    }
    private void gap(LinearLayout parent,int h) {View v=new View(this);parent.addView(v,lp(1,h));}
    private void subtitle(LinearLayout p,String s) {TextView t=text(s,13,false);t.setTextColor(MUTED);p.addView(t,lp(-1,-2));}
    private MaterialButton button(String title,boolean primary,Runnable action) {
        MaterialButton b=new MaterialButton(this);b.setText(title);b.setTextSize(13);b.setAllCaps(false);
        b.setTextColor(primary?Color.WHITE:NAVY);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary?NAVY:PALE));
        b.setCornerRadius(dp(14));b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));
        b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(12),0,dp(12),0);
        b.setInsetTop(dp(3));b.setInsetBottom(dp(3));b.setOnClickListener(v->action.run());return b;
    }
    private ImageView image(int resource,int w,int h,String description) {
        ImageView im=new ImageView(this);im.setImageResource(resource);im.setScaleType(ImageView.ScaleType.CENTER_CROP);
        im.setBackground(background(PALE,7,false));im.setClipToOutline(true);im.setContentDescription(description);
        im.setLayoutParams(lp(w,h));return im;
    }
    private ImageView icon(int resource,int size) {
        ImageView im=new ImageView(this);im.setImageResource(resource);im.setScaleType(ImageView.ScaleType.FIT_CENTER);
        im.setLayoutParams(lp(size,size));im.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);return im;
    }
    private void toast(String s) {Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private void go(String target) {hideKeyboard();history.push(screen+"|"+productId+"|"+units+"|"+sellerIndex);screen=target;render();}
    private void tab(String target) {
        hideKeyboard();history.clear();screen=target;
        if(target.equals("search")) {favoritesOnly=false;category="Todos";query="";}
        render();
    }
    private void hideKeyboard() {
        View f=getCurrentFocus();if(f!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(f.getWindowToken(),0);
    }
    private void openProduct(Catalog.Product p) {
        hideKeyboard();history.push(screen+"|"+productId+"|"+units+"|"+sellerIndex);
        productId=p.id;units=2;screen="product";render();
    }
    private void browse(String cat,String term) {category=cat;query=term;favoritesOnly=false;go("search");}
    private void render() {
        stopRecommendations();recommendationItems=null;
        if(body!=null)body.animate().cancel();
        container.removeAllViews();
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);
        body=column();body.setPadding(dp(25),dp(20),dp(25),dp(24));
        scroll.addView(body,new ScrollView.LayoutParams(-1,-2));container.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        switch(screen) {
            case "home":home();break;case "search":search();break;case "product":product();break;
            case "cart":cart();break;case "checkout":checkout();break;case "orders":orders();break;
            case "order":order();break;case "profile":profile();break;case "seller":seller();break;
            case "sellers":sellers();break;case "chat":chat();break;default:screen="home";home();
        }
        bottomNav();
        body.setAlpha(0f);body.setTranslationY(dp(8));
        body.animate().alpha(1f).translationY(0f).setDuration(200).start();
        scheduleRecommendations();
    }
    private void header() {
        LinearLayout top=row();
        ImageView logo=image(R.drawable.brand_logo,105,45,"Cais");
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);logo.setBackgroundColor(Color.WHITE);
        top.addView(logo);
        View spacer=new View(this);top.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
        LinearLayout loc=row();loc.addView(icon(R.drawable.fg_981d4,16));
        TextView location=text(store.get("city","Manaus - AM"),10,true);loc.addView(location);
        loc.addView(icon(R.drawable.fg_fecec,8));loc.setMinimumHeight(dp(48));
        loc.setContentDescription("Alterar localização");loc.setOnClickListener(v->locationDialog());top.addView(loc);
        body.addView(top,lp(-1,50));gap(body,5);
    }
    private void locationDialog() {
        new AlertDialog.Builder(this).setTitle("Sua região").setMessage("O catálogo de demonstração atende Manaus e região.")
            .setItems(new String[]{"Manaus - AM","Iranduba - AM","Manacapuru - AM"},(d,i)->{
                store.put("city",new String[]{"Manaus - AM","Iranduba - AM","Manacapuru - AM"}[i]);render();
            }).show();
    }
    private void pageTitle(String title,String back) {
        LinearLayout r=row();ImageView b=icon(R.drawable.fg_6c5f1,48);
        b.setRotation(180);pad(b,16);b.setBackground(background(PALE,14,false));b.setContentDescription("Voltar");
        b.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        b.setOnClickListener(v->{if(back!=null)go(back);else getOnBackPressedDispatcher().onBackPressed();});
        r.addView(b,lp(48,48));TextView t=text(title,22,true);t.setPadding(dp(12),0,0,0);
        r.addView(t,new LinearLayout.LayoutParams(0,-2,1));body.addView(r,lp(-1,-2));gap(body,14);
    }
    private void searchBar(boolean editable) {
        LinearLayout r=row();LinearLayout field=row();field.setBackground(background(PALE,5,false));
        ImageView searchIcon=icon(R.drawable.fg_41ed4,18);searchIcon.setPadding(dp(3),0,0,0);field.addView(searchIcon,lp(28,40));
        if(editable) {
            EditText input=new EditText(this);input.setSingleLine(true);input.setTextSize(11);input.setTextColor(NAVY);
            input.setHint("Buscar peixe, camarão, vendedor...");input.setBackgroundColor(Color.TRANSPARENT);
            input.setContentDescription("Buscar produtos");input.setText(query);input.setSelectAllOnFocus(true);
            field.addView(input,new LinearLayout.LayoutParams(0,dp(40),1));
            input.addTextChangedListener(new TextWatcher(){
                public void beforeTextChanged(CharSequence s,int start,int count,int after){}
                public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();updateResults();}
                public void afterTextChanged(Editable e){}
            });
        } else {
            TextView t=text("Buscar peixe, camarão, vendedor...",10,false);t.setTextColor(MUTED);
            t.setGravity(Gravity.CENTER_VERTICAL);t.setSingleLine(true);t.setIncludeFontPadding(false);
            field.addView(t,new LinearLayout.LayoutParams(0,dp(40),1));
            field.setOnClickListener(v->browse("Todos",""));field.setContentDescription("Buscar produtos");
        }
        r.addView(field,new LinearLayout.LayoutParams(0,dp(40),1));
        ImageView filter=icon(R.drawable.fg_d86c2,20);pad(filter,10);filter.setBackground(background(PALE,5,true));
        LinearLayout.LayoutParams fl=lp(40,40);fl.leftMargin=dp(8);r.addView(filter,fl);
        filter.setContentDescription("Filtros e ordenação");filter.setOnClickListener(v->filters());body.addView(r,lp(-1,-2));gap(body,8);
    }
    private void filters() {
        String[] choices={"Relevância","Menor preço","Maior avaliação","Somente favoritos"};
        new AlertDialog.Builder(this).setTitle("Filtrar e ordenar").setSingleChoiceItems(choices,favoritesOnly?3:sort,(d,i)->{
            favoritesOnly=i==3;sort=i==3?0:i;
            if(screen.equals("search"))render();else go("search");d.dismiss();
        }).setNegativeButton("Limpar",(d,i)->{sort=0;favoritesOnly=false;category="Todos";query="";if(screen.equals("search"))render();else go("search");}).show();
    }
    private void categories(boolean isHome) {
        HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);
        LinearLayout r=row();String[] cats=isHome?new String[]{"Peixe de Rio","Camarão","Frutos do Mar","Promoções"}:new String[]{"Todos","Peixes","Frutos do Mar","Promoções"};
        int[] icons=isHome?new int[]{R.drawable.fg_c7997,R.drawable.fg_8eced,R.drawable.fg_97a0f,R.drawable.fg_99683}:new int[]{R.drawable.fg_f1b77,R.drawable.fg_c7997,R.drawable.fg_0ef51,R.drawable.fg_99683};
        for(int i=0;i<cats.length;i++) {
            String cat=cats[i], mapped=cat.equals("Peixe de Rio")?"Peixes":cat;
            boolean selected=isHome?i==0:category.equals(cat);
            LinearLayout chip=row();chip.setGravity(Gravity.CENTER);chip.setPadding(dp(7),0,dp(7),0);
            chip.setBackground(background(selected?NAVY:PALE,10,true));ImageView ic=icon(icons[i],12);
            if(selected)ic.setImageTintList(android.content.res.ColorStateList.valueOf(CYAN));chip.addView(ic);
            TextView t=text(cat,9,true);t.setTextColor(selected?Color.WHITE:NAVY);t.setPadding(dp(5),0,0,0);chip.addView(t);
            LinearLayout.LayoutParams params=lp(-2,36);if(i>0)params.leftMargin=dp(12);r.addView(chip,params);
            chip.setContentDescription("Categoria "+cat);chip.setOnClickListener(v->{
                if(isHome)browse(mapped,"");else{category=mapped;render();}
            });
        }
        sc.addView(r);body.addView(sc,lp(-1,38));gap(body,12);
    }
    private void section(String title,Runnable action) {
        LinearLayout r=row();TextView t=text(title,16,true);r.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        TextView all=text("Ver todos",10,false);all.setTextColor(Color.rgb(2,115,165));all.setGravity(Gravity.CENTER);
        all.setMinimumHeight(dp(44));all.setOnClickListener(v->action.run());r.addView(all,lp(58,44));r.addView(icon(R.drawable.fg_6c5f1,12));
        r.setOnClickListener(v->action.run());body.addView(r,lp(-1,44));gap(body,5);
    }
    private void home() {
        header();searchBar(false);categories(true);
        int[] banners={R.drawable.banner_figma,R.drawable.banner_two,R.drawable.banner_three};
        FrameLayout banner=new FrameLayout(this);ImageView photo=image(banners[bannerIndex],-1,125,"Oferta de pescados frescos. Toque para comprar.");
        banner.addView(photo,new FrameLayout.LayoutParams(-1,dp(125)));photo.setOnClickListener(v->openProduct(Catalog.PRODUCTS.get(0)));
        TextView next=text("›",22,true);next.setTextColor(Color.WHITE);next.setGravity(Gravity.CENTER);
        next.setBackground(background(0x77083359,20,false));FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(dp(32),dp(44),Gravity.END|Gravity.CENTER_VERTICAL);
        banner.addView(next,np);next.setContentDescription("Próximo banner");next.setOnClickListener(v->{bannerIndex=(bannerIndex+1)%3;photo.setImageResource(banners[bannerIndex]);});
        body.addView(banner,lp(-1,125));gap(body,12);
        section("Mais procurados",()->browse("Todos",""));productCarousel(false);
        gap(body,20);section("Vendedores em destaque",()->go("sellers"));sellerCarousel();
        gap(body,20);section("Recomendados para você",()->browse("Todos",""));productCarousel(true);
        if(!store.cart.isEmpty()){gap(body,14);body.addView(button("Ver carrinho · "+Catalog.money(store.subtotal()),true,()->go("cart")),lp(-1,52));}
    }
    private void productCarousel(boolean recommended) {
        HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);LinearLayout r=row();
        if(recommended)recommendationItems=r;
        populateProductCards(r,recommended);
        sc.addView(r);body.addView(sc,lp(-1,-2));
    }
    private void populateProductCards(LinearLayout r,boolean recommended) {
        r.removeAllViews();
        int width=Math.max(86,(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density-62)/4);
        int[] indices=new int[]{0,1,2,0,4,6};
        if(recommended) {
            indices=new int[RECOMMENDED_PRODUCTS.length];
            for(int i=0;i<indices.length;i++)
                indices[i]=RECOMMENDED_PRODUCTS[(i+recommendationOffset)%RECOMMENDED_PRODUCTS.length];
        }
        for(int index:indices){Catalog.Product p=Catalog.PRODUCTS.get(index);LinearLayout card=column();pad(card,3);
            card.setBackground(background(PALE,6,false));card.setElevation(dp(2));
            FrameLayout photo=new FrameLayout(this);ImageView im=image(p.homeImage,-1,49,p.name);photo.addView(im,new FrameLayout.LayoutParams(-1,dp(49)));
            if(p.id.equals("pirarucu")) {
                im.setScaleType(ImageView.ScaleType.MATRIX);
                im.addOnLayoutChangeListener((view,left,top,right,bottom,ol,ot,or,ob)->{
                    int w=im.getWidth(),h=im.getHeight();
                    float dw=im.getDrawable().getIntrinsicWidth(),dh=im.getDrawable().getIntrinsicHeight();
                    android.graphics.Matrix matrix=new android.graphics.Matrix();
                    matrix.postTranslate(-dw/2,-dh/2);matrix.postRotate(90);
                    float scale=Math.max(w/dh,h/dw);matrix.postScale(scale,scale);matrix.postTranslate(w/2f,h/2f);
                    im.setImageMatrix(matrix);
                });
            }
            ImageView fav=icon(R.drawable.fg_5d11b,18);FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(26),dp(32),Gravity.TOP|Gravity.END);photo.addView(fav,fp);
            if(store.favorites.contains(p.id))fav.setImageTintList(android.content.res.ColorStateList.valueOf(CYAN));
            fav.setContentDescription("Favoritar "+p.name);fav.setOnClickListener(v->{store.toggleFavorite(p.id);fav.setImageTintList(android.content.res.ColorStateList.valueOf(store.favorites.contains(p.id)?CYAN:NAVY));});
            card.addView(photo,lp(-1,49));gap(card,3);label(card,p.name,10,true);label(card,p.origin,8,false);
            TextView rating=text("★ "+p.rating+" (214)",6,false);rating.setTextColor(Color.rgb(204,130,0));card.addView(rating);gap(card,8);
            label(card,Catalog.money(p.cents)+"/kg",11,true);gap(card,5);
            LinearLayout seller=row();seller.setBackground(background(0x55083359,4,false));seller.addView(image(p.seller().image,25,27,p.seller().name));
            LinearLayout name=column();label(name,p.seller().shop,5,false);label(name,p.seller().name,6,true);label(name,"★ "+p.seller().rating,5,false);
            seller.addView(name,new LinearLayout.LayoutParams(0,-2,1));card.addView(seller,lp(-1,29));
            seller.setOnClickListener(v->{sellerIndex=p.seller;go("seller");});card.setOnClickListener(v->openProduct(p));
            LinearLayout.LayoutParams cp=lp(width,-2);cp.rightMargin=dp(6);cp.bottomMargin=dp(5);r.addView(card,cp);
        }
    }
    private void sellerCarousel() {
        HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);LinearLayout r=row();
        int width=Math.max(86,(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density-62)/4);
        for(int i=0;i<Catalog.SELLERS.size();i++) {
            final int index=i;Catalog.Seller s=Catalog.SELLERS.get(i);LinearLayout c=column();pad(c,3);c.setBackground(background(PALE,6,false));c.setElevation(dp(2));
            c.addView(image(s.featuredImage,-1,86,s.name));gap(c,6);label(c,s.name,10,true);label(c,s.shop,7,false);gap(c,5);
            label(c,"Centro - AM · ★ "+s.rating,6,false);gap(c,8);c.setOnClickListener(v->{sellerIndex=index;go("seller");});
            LinearLayout.LayoutParams cp=lp(width,-2);cp.rightMargin=dp(6);cp.bottomMargin=dp(5);r.addView(c,cp);
        }
        sc.addView(r);body.addView(sc,lp(-1,-2));
    }
    private void search() {
        header();searchBar(true);categories(false);
        if(favoritesOnly){label(body,"Seus favoritos",18,true);gap(body,12);}
        results=column();pad(results,10);results.setBackground(background(0x70E1EAF2,20,false));body.addView(results,lp(-1,-2));updateResults();
        if(!store.cart.isEmpty()){gap(body,16);body.addView(button("Ver carrinho · "+Catalog.money(store.subtotal()),true,()->go("cart")),lp(-1,52));}
    }
    private void updateResults() {
        if(results==null || !screen.equals("search"))return;results.removeAllViews();
        List<Catalog.Product> found=new ArrayList<>();
        for(Catalog.Product p:Catalog.PRODUCTS)if(Catalog.matches(p,query,category,favoritesOnly,store.favorites.contains(p.id)))found.add(p);
        if(sort==1)found.sort(Comparator.comparingInt(p->p.cents));
        if(sort==2)found.sort((a,b)->b.rating.compareTo(a.rating));
        if(found.isEmpty()){gap(results,24);label(results,"Nenhum produto encontrado",18,true);subtitle(results,"Tente outro nome, categoria ou vendedor.");gap(results,24);}
        for(Catalog.Product p:found){gap(results,16);results.addView(productRow(p));gap(results,12);}
    }
    private View productRow(Catalog.Product p) {
        LinearLayout card=row();pad(card,10);card.setBackground(background(Color.WHITE,12,true));card.setElevation(dp(3));
        card.addView(image(p.image,104,117,p.name));LinearLayout info=column();info.setPadding(dp(14),0,0,0);
        LinearLayout title=row();TextView name=text(p.name,16,true);title.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        ImageView favorite=icon(R.drawable.fg_bedf3,18);favorite.setPadding(dp(9),dp(9),dp(9),dp(9));
        if(store.favorites.contains(p.id))favorite.setImageTintList(android.content.res.ColorStateList.valueOf(CYAN));
        favorite.setContentDescription("Favoritar "+p.name);favorite.setOnClickListener(v->{store.toggleFavorite(p.id);updateResults();});title.addView(favorite,lp(36,36));info.addView(title,lp(-1,30));
        subtitle(info,p.origin+" · Fresco");label(info,"★ "+p.rating,9,true);label(info,Catalog.money(p.cents)+"/kg",22,true);gap(info,10);
        info.addView(sellerStrip(p.seller(),p.seller,8));card.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        card.setOnClickListener(v->openProduct(p));return card;
    }
    private View sellerStrip(Catalog.Seller s,int index,int font) {
        LinearLayout r=row();r.setBackground(background(0x252DCBDC,5,false));r.addView(image(s.image,29,29,s.name));
        LinearLayout names=column();names.setPadding(dp(4),0,0,0);label(names,s.shop,Math.max(6,font-2),false);label(names,s.name,font,true);label(names,"★ "+s.rating,6,false);
        r.addView(names,new LinearLayout.LayoutParams(0,-2,1));r.addView(icon(R.drawable.fg_90162,6));
        r.setContentDescription("Ver loja de "+s.name);r.setOnClickListener(v->{sellerIndex=index;go("seller");});return r;
    }
    private void product() {
        Catalog.Product p=Catalog.find(productId);if(p==null){tab("home");return;}
        pageTitle("Detalhes do produto",null);body.addView(image(p.image,-1,240,p.name));gap(body,20);
        LinearLayout title=row();title.addView(text(p.name,30,true),new LinearLayout.LayoutParams(0,-2,1));
        ImageView fav=icon(R.drawable.fg_bedf3,48);pad(fav,14);fav.setBackground(background(PALE,14,false));
        if(store.favorites.contains(p.id))fav.setImageTintList(android.content.res.ColorStateList.valueOf(CYAN));
        fav.setContentDescription("Favoritar "+p.name);fav.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        fav.setOnClickListener(v->{store.toggleFavorite(p.id);render();});title.addView(fav,lp(48,48));body.addView(title);
        subtitle(body,p.origin+" · Fresco");gap(body,8);label(body,"★ "+p.rating+" (214 avaliações)",16,true);gap(body,12);
        label(body,Catalog.money(p.cents)+"/kg",28,true);gap(body,20);label(body,"Vendedor",20,true);gap(body,10);body.addView(sellerStrip(p.seller(),p.seller,14));
        gap(body,20);label(body,"Sobre o produto",20,true);subtitle(body,p.name+" fresco, selecionado para sua mesa. Combine a limpeza e o corte com o vendedor na retirada.");
        gap(body,10);subtitle(body,"Peixe fresco · Limpeza grátis · Corte na hora");gap(body,22);label(body,"Escolha a quantidade",20,true);
        gap(body,8);LinearLayout controls=row();TextView quantity=text(Catalog.kilos(units),22,true);quantity.setGravity(Gravity.CENTER);
        TextView total=text(Catalog.money(Catalog.lineTotal(p.cents,units)),26,true);
        controls.addView(button("−",false,()->{units=Math.max(1,units-1);quantity.setText(Catalog.kilos(units));total.setText(Catalog.money(Catalog.lineTotal(p.cents,units)));}),lp(60,50));
        controls.addView(quantity,new LinearLayout.LayoutParams(0,dp(50),1));
        controls.addView(button("+",true,()->{units=Math.min(40,units+1);quantity.setText(Catalog.kilos(units));total.setText(Catalog.money(Catalog.lineTotal(p.cents,units)));}),lp(60,50));body.addView(controls);
        gap(body,12);label(body,"Preço total",14,false);body.addView(total);gap(body,12);
        body.addView(button("Adicionar ao carrinho",true,()->{store.add(p.id,units);go("cart");}),lp(-1,54));
        body.addView(button("Conversar com o vendedor",false,()->{sellerIndex=p.seller;go("chat");}),lp(-1,50));
    }
    private void cart() {
        pageTitle("Seu carrinho",null);
        if(store.cart.isEmpty()){label(body,"Seu carrinho está vazio",22,true);subtitle(body,"Escolha pescados frescos para começar.");gap(body,20);body.addView(button("Explorar produtos",true,()->browse("Todos","")));return;}
        for(Map.Entry<String,Integer> entry:new ArrayList<>(store.cart.entrySet())) {
            Catalog.Product p=Catalog.find(entry.getKey());int count=entry.getValue();LinearLayout c=column();pad(c,12);c.setBackground(background(Color.WHITE,12,true));
            LinearLayout r=row();r.addView(image(p.image,76,70,p.name));LinearLayout info=column();pad(info,10);label(info,p.name,18,true);subtitle(info,p.seller().shop);label(info,Catalog.money(p.cents)+"/kg",14,false);r.addView(info,new LinearLayout.LayoutParams(0,-2,1));c.addView(r);
            LinearLayout control=row();control.addView(button("−",false,()->{if(count>1)store.cart.put(p.id,count-1);else store.cart.remove(p.id);store.save();render();}),lp(52,48));
            TextView q=text(Catalog.kilos(count),16,true);q.setGravity(Gravity.CENTER);control.addView(q,new LinearLayout.LayoutParams(0,dp(48),1));
            control.addView(button("+",false,()->{store.cart.put(p.id,Math.min(40,count+1));store.save();render();}),lp(52,48));
            control.addView(button("Remover",false,()->{store.cart.remove(p.id);store.save();render();}),lp(100,48));c.addView(control);
            label(c,Catalog.money(Catalog.lineTotal(p.cents,count)),20,true);body.addView(c);gap(body,14);
        }
        label(body,"Subtotal: "+Catalog.money(store.subtotal()),24,true);subtitle(body,"Retirada grátis. Entrega: R$ 8,00 por pedido.");gap(body,16);
        body.addView(button("Continuar para pagamento",true,()->go("checkout")),lp(-1,54));
        body.addView(button("Adicionar mais produtos",false,()->browse("Todos","")),lp(-1,50));
    }
    private RadioGroup choices(String[] titles,int checked,java.util.function.IntConsumer change) {
        RadioGroup group=new RadioGroup(this);group.setOrientation(RadioGroup.VERTICAL);
        for(int i=0;i<titles.length;i++){RadioButton b=new RadioButton(this);b.setId(View.generateViewId());b.setText(titles[i]);b.setTextColor(NAVY);b.setTextSize(14);b.setMinHeight(dp(48));group.addView(b,lp(-1,-2));}
        ((RadioButton)group.getChildAt(checked)).setChecked(true);
        group.setOnCheckedChangeListener((g,id)->{for(int i=0;i<g.getChildCount();i++)if(g.getChildAt(i).getId()==id)change.accept(i);});return group;
    }
    private EditText field(String hint,String value,boolean multi) {
        EditText e=new EditText(this);e.setTextSize(15);e.setTextColor(NAVY);e.setHint(hint);e.setContentDescription(hint);
        e.setText(value);e.setBackground(background(0x70E1EAF2,10,true));pad(e,12);e.setSingleLine(!multi);
        e.setInputType(InputType.TYPE_CLASS_TEXT|(multi?InputType.TYPE_TEXT_FLAG_MULTI_LINE:InputType.TYPE_TEXT_FLAG_CAP_SENTENCES));return e;
    }
    private void watch(EditText e,java.util.function.Consumer<String> c) {
        e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){c.accept(s.toString());}public void afterTextChanged(Editable v){}});
    }
    private void checkout() {
        if(store.cart.isEmpty()){cart();return;}pageTitle("Pagamento / Negociação",null);
        subtitle(body,"Compra de demonstração · Nenhuma cobrança real");gap(body,16);
        for(Map.Entry<String,Integer> e:store.cart.entrySet()){
            Catalog.Product p=Catalog.find(e.getKey());label(body,p.name+" · "+Catalog.kilos(e.getValue()),17,true);subtitle(body,p.seller().shop);label(body,Catalog.money(Catalog.lineTotal(p.cents,e.getValue())),18,true);gap(body,12);
        }
        label(body,"Negociação (opcional)",20,true);subtitle(body,"Converse sobre limpeza e retirada. As respostas são simuladas.");
        body.addView(button("Conversar com o vendedor",false,()->{sellerIndex=Catalog.find(store.cart.keySet().iterator().next()).seller;go("chat");}),lp(-1,50));
        EditText note=field("Observações: limpeza, corte, retirada...",noteDraft,true);watch(note,v->noteDraft=v);body.addView(note,lp(-1,-2));gap(body,20);
        label(body,"Forma de entrega",20,true);
        TextView total=text("Total: "+Catalog.money(store.subtotal()+(delivery?800:0)),24,true);
        EditText address=field("Endereço completo para entrega",addressDraft,true);address.setVisibility(delivery?View.VISIBLE:View.GONE);
        watch(address,v->addressDraft=v);
        body.addView(choices(new String[]{"Retirar na feira · Grátis","Entrega na sua região · R$ 8,00"},delivery?1:0,i->{delivery=i==1;address.setVisibility(delivery?View.VISIBLE:View.GONE);total.setText("Total: "+Catalog.money(store.subtotal()+(delivery?800:0)));}));
        body.addView(address,lp(-1,-2));gap(body,18);label(body,"Forma de pagamento",20,true);
        String[] payments={"PIX","Cartão de crédito","Dinheiro na entrega/retirada"};int pi=Arrays.asList(payments).indexOf(payment);
        body.addView(choices(payments,Math.max(0,pi),i->payment=payments[i]));gap(body,18);body.addView(total);gap(body,8);
        body.addView(button("Confirmar compra de demonstração",true,()->{
            if(delivery && addressDraft.trim().length()<10){address.setError("Informe rua, número e bairro");address.requestFocus();return;}
            new AlertDialog.Builder(this).setTitle("Confirmar pedido local").setMessage("Total: "+Catalog.money(store.subtotal()+(delivery?800:0))+"\n"+payment+"\nEste pedido é salvo apenas neste aparelho e não é enviado a uma loja.")
                .setNegativeButton("Voltar",null).setPositiveButton("Confirmar",(d,i)->{
                    try{JSONObject placed=store.placeOrder(delivery,payment,addressDraft.trim(),noteDraft.trim());store.put("address",addressDraft.trim());orderId=placed.getString("id");noteDraft="";history.clear();screen="order";render();toast("Pedido de demonstração criado!");}
                    catch(JSONException|IllegalStateException e){toast("Não foi possível criar o pedido. Tente novamente.");}
                }).show();
        }),lp(-1,58));
    }
    private void orders() {
        header();label(body,"Pedidos",28,true);gap(body,10);
        HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);LinearLayout tabs=row();
        for(String f:new String[]{"Todos","Em andamento","Concluídos","Cancelados"}){MaterialButton b=button(f,orderFilter.equals(f),()->{orderFilter=f;render();});tabs.addView(b,lp(-2,50));}sc.addView(tabs);body.addView(sc);gap(body,12);
        int shown=0;
        for(int i=0;i<store.orders.length();i++){JSONObject o=store.orders.optJSONObject(i);if(o==null)continue;String s=o.optString("status");
            if(orderFilter.equals("Concluídos")&&!s.equals("Concluído")||orderFilter.equals("Cancelados")&&!s.equals("Cancelado")||orderFilter.equals("Em andamento")&&(s.equals("Concluído")||s.equals("Cancelado")))continue;
            shown++;LinearLayout c=column();pad(c,14);c.setBackground(background(Color.WHITE,12,true));label(c,"#"+o.optString("id"),15,true);
            subtitle(c,new SimpleDateFormat("dd/MM/yyyy · HH:mm",Catalog.BR).format(new Date(o.optLong("created"))));gap(c,8);label(c,s,16,true);
            JSONArray lines=o.optJSONArray("items");if(lines!=null&&lines.length()>0){Catalog.Product p=Catalog.find(lines.optJSONObject(0).optString("product"));if(p!=null){gap(c,8);LinearLayout r=row();r.addView(image(p.image,66,60,p.name));LinearLayout details=column();pad(details,8);label(details,p.name,17,true);subtitle(details,lines.length()+" produto(s)");r.addView(details);c.addView(r);}}
            gap(c,8);label(c,Catalog.money(o.optLong("total")),21,true);subtitle(c,o.optBoolean("delivery")?"Entrega na sua região":"Retirada na feira");
            c.addView(button("Acompanhar pedido",false,()->{orderId=o.optString("id");go("order");}),lp(-1,50));body.addView(c);gap(body,14);
        }
        if(shown==0){gap(body,30);label(body,"Nenhum pedido por aqui",22,true);subtitle(body,"Os pedidos feitos no checkout ficam salvos neste aparelho.");body.addView(button("Explorar produtos",true,()->browse("Todos","")),lp(-1,50));}
        if(!store.cart.isEmpty())body.addView(button("Abrir carrinho · "+Catalog.money(store.subtotal()),true,()->go("cart")),lp(-1,52));
    }
    private JSONObject currentOrder(){for(int i=0;i<store.orders.length();i++){JSONObject o=store.orders.optJSONObject(i);if(o!=null&&o.optString("id").equals(orderId))return o;}return null;}
    private void order() {
        JSONObject o=currentOrder();if(o==null){screen="orders";orders();return;}pageTitle("Seu pedido",null);
        label(body,"#"+o.optString("id"),16,true);subtitle(body,"Pedido de demonstração salvo no aparelho");gap(body,16);
        label(body,o.optString("status"),24,true);gap(body,16);JSONArray lines=o.optJSONArray("items");
        for(int i=0;lines!=null&&i<lines.length();i++){JSONObject l=lines.optJSONObject(i);Catalog.Product p=Catalog.find(l.optString("product"));if(p==null)continue;
            LinearLayout r=row();r.addView(image(p.image,88,80,p.name));LinearLayout info=column();pad(info,10);label(info,p.name,18,true);subtitle(info,Catalog.kilos(l.optInt("units"))+" × "+Catalog.money(l.optInt("cents"))+"/kg");label(info,Catalog.money(Catalog.lineTotal(l.optInt("cents"),l.optInt("units"))),18,true);r.addView(info,new LinearLayout.LayoutParams(0,-2,1));body.addView(r);gap(body,12);
        }
        subtitle(body,"Entrega: "+(o.optBoolean("delivery")?o.optString("address"):"Retirada na feira"));subtitle(body,"Pagamento: "+o.optString("payment"));
        if(!o.optString("note").isEmpty())subtitle(body,"Observações: "+o.optString("note"));
        gap(body,14);label(body,"Subtotal: "+Catalog.money(o.optLong("subtotal")),17,false);label(body,"Entrega: "+Catalog.money(o.optLong("fee")),17,false);label(body,"Total: "+Catalog.money(o.optLong("total")),26,true);gap(body,20);
        String status=o.optString("status");
        if(!status.equals("Concluído")&&!status.equals("Cancelado")) {
            body.addView(button("Simular avanço do pedido",false,()->{setStatus(o,status.equals("Em preparação")?(o.optBoolean("delivery")?"Em entrega":"Pronto para retirada"):"Concluído");}),lp(-1,52));
            body.addView(button("Cancelar pedido",false,()->new AlertDialog.Builder(this).setTitle("Cancelar este pedido?").setNegativeButton("Voltar",null).setPositiveButton("Cancelar pedido",(d,i)->setStatus(o,"Cancelado")).show()),lp(-1,50));
        }
        body.addView(button("Comprar novamente",true,()->{
            for(int i=0;lines!=null&&i<lines.length();i++){JSONObject l=lines.optJSONObject(i);if(Catalog.find(l.optString("product"))!=null)store.add(l.optString("product"),l.optInt("units",2));}go("cart");
        }),lp(-1,52));body.addView(button("Ver todos os pedidos",false,()->tab("orders")),lp(-1,50));
    }
    private void setStatus(JSONObject o,String status){try{o.put("status",status);store.save();render();}catch(JSONException e){toast("Não foi possível atualizar o pedido.");}}
    private void profile() {
        header();label(body,"Seu perfil",28,true);subtitle(body,"Perfil local para demonstração");gap(body,22);
        EditText name=field("Seu nome",store.get("name",""),false);body.addView(name,lp(-1,52));gap(body,12);
        EditText phone=field("Telefone",store.get("phone",""),false);phone.setInputType(InputType.TYPE_CLASS_PHONE);body.addView(phone,lp(-1,52));gap(body,12);
        EditText address=field("Endereço",store.get("address",""),true);body.addView(address,lp(-1,-2));gap(body,12);
        body.addView(button("Salvar perfil",true,()->{
            if(name.getText().toString().trim().isEmpty()){name.setError("Informe seu nome");return;}
            store.put("name",name.getText().toString().trim());store.put("phone",phone.getText().toString().trim());store.put("address",address.getText().toString().trim());addressDraft=address.getText().toString().trim();hideKeyboard();toast("Perfil salvo no aparelho");
        }),lp(-1,52));gap(body,20);
        body.addView(button("Meus favoritos ("+store.favorites.size()+")",false,()->{query="";category="Todos";favoritesOnly=true;go("search");}),lp(-1,52));
        body.addView(button("Meus pedidos",false,()->tab("orders")),lp(-1,52));body.addView(button("Meu carrinho",false,()->go("cart")),lp(-1,52));gap(body,20);
        subtitle(body,"Cais · Pescados de Manaus\nCatálogo e vendedores de exemplo. Compras e mensagens ficam neste aparelho.");
    }
    private void sellers() {pageTitle("Vendedores",null);for(int i=0;i<Catalog.SELLERS.size();i++){int n=i;Catalog.Seller s=Catalog.SELLERS.get(i);body.addView(sellerStrip(s,n,16),lp(-1,68));gap(body,14);}}
    private void seller() {
        Catalog.Seller s=Catalog.SELLERS.get(sellerIndex);pageTitle("Loja do vendedor",null);body.addView(image(s.image,-1,210,s.name));gap(body,16);
        label(body,s.name,28,true);label(body,s.shop,20,false);subtitle(body,"★ "+s.rating+" · Centro, Manaus - AM");gap(body,14);
        body.addView(button("Conversar com "+s.name.split(" ")[0],true,()->go("chat")),lp(-1,52));gap(body,18);label(body,"Produtos desta loja",20,true);gap(body,12);
        for(Catalog.Product p:Catalog.PRODUCTS)if(p.seller==sellerIndex){body.addView(productRow(p));gap(body,18);}
    }
    private void chat() {
        Catalog.Seller s=Catalog.SELLERS.get(sellerIndex);pageTitle("Conversar",null);body.addView(sellerStrip(s,sellerIndex,14));gap(body,12);
        subtitle(body,"Conversa de demonstração · Respostas automáticas locais");gap(body,16);JSONArray messages=store.messages(sellerIndex);
        if(messages.length()==0){subtitle(body,"Combine limpeza, corte e retirada com "+s.name+".");gap(body,16);}
        for(int i=0;i<messages.length();i++){JSONObject m=messages.optJSONObject(i);if(m==null)continue;TextView t=text(m.optString("text"),15,false);pad(t,12);t.setBackground(background(m.optBoolean("mine")?0x552DCBDC:PALE,12,false));body.addView(t,lp(-1,-2));gap(body,10);}
        EditText input=field("Digite uma mensagem...","",true);body.addView(input,lp(-1,-2));gap(body,6);
        body.addView(button("Enviar mensagem",true,()->{
            String msg=input.getText().toString().trim();if(msg.isEmpty()){input.setError("Digite sua mensagem");return;}
            try{store.send(sellerIndex,msg);hideKeyboard();render();}catch(JSONException e){toast("Não foi possível salvar a mensagem.");}
        }),lp(-1,52));
    }
    private void bottomNav() {
        navigation.removeAllViews();String[] labels={"Início","Buscar","Pedidos","Perfil"},targets={"home","search","orders","profile"};
        int[] icons={screen.equals("home")?R.drawable.fg_a2007:R.drawable.fg_2c680,R.drawable.fg_c52d5,R.drawable.fg_4530c,R.drawable.fg_b0fc8};
        for(int i=0;i<targets.length;i++){String target=targets[i];LinearLayout item=column();item.setGravity(Gravity.CENTER);item.setContentDescription(labels[i]);
            ImageView im=icon(icons[i],25);im.setImageTintList(android.content.res.ColorStateList.valueOf(screen.equals(target)?NAVY:MUTED));item.addView(im);
            TextView t=text(labels[i],9,screen.equals(target));t.setGravity(Gravity.CENTER);item.addView(t);navigation.addView(item,new LinearLayout.LayoutParams(0,-1,1));item.setOnClickListener(v->tab(target));
        }
    }
}
