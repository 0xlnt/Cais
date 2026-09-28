package com.projeto.caisapp;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONObject;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PurchaseFlowTest {
    private Context context;
    @Before public void clearDemoStateOnTestDevice() {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("cais_local_v1",Context.MODE_PRIVATE).edit().clear().commit();
    }
    @Test public void searchesPurchasesAndKeepsOrderAfterRecreation() {
        try(ActivityScenario<MainActivity> scenario=ActivityScenario.launch(MainActivity.class)) {
            onView(withContentDescription("Buscar")).perform(click());
            onView(withContentDescription("Buscar produtos")).perform(replaceText("matrinxa"),closeSoftKeyboard());
            onView(withText("Matrinxã")).perform(scrollTo(),click());
            onView(withText("+")).perform(scrollTo(),click());
            onView(withText("Adicionar ao carrinho")).perform(scrollTo(),click());
            onView(withText("1,5 kg")).check(matches(isDisplayed()));
            onView(withText("Continuar para pagamento")).perform(scrollTo(),click());
            onView(withText("Confirmar compra de demonstração")).perform(scrollTo(),click());
            onView(withText("Confirmar")).perform(click());
            onView(withText("Em preparação")).check(matches(isDisplayed()));
            LocalStore saved=new LocalStore(context);
            assertEquals(1,saved.orders.length());
            assertTrue(saved.cart.isEmpty());
            assertEquals(4838,saved.orders.optJSONObject(0).optLong("total"));
            scenario.recreate();
            onView(withText("Em preparação")).check(matches(isDisplayed()));
            onView(withText("Simular avanço do pedido")).perform(scrollTo(),click());
            onView(withText("Pronto para retirada")).check(matches(isDisplayed()));
            onView(withText("Simular avanço do pedido")).perform(scrollTo(),click());
            onView(withText("Concluído")).check(matches(isDisplayed()));
        }
    }
    @Test public void persistsCartFavoritesChatAndDeliveryFee() throws Exception {
        LocalStore store=new LocalStore(context);
        store.add("tambaqui",3);store.toggleFavorite("tambaqui");store.send(0,"Pode limpar?");
        LocalStore restored=new LocalStore(context);
        assertEquals(Integer.valueOf(3),restored.cart.get("tambaqui"));
        assertTrue(restored.favorites.contains("tambaqui"));
        assertEquals(2,restored.messages(0).length());
        JSONObject order=restored.placeOrder(true,"PIX","Rua Teste, 10, Centro","");
        assertEquals(2732,order.getLong("subtotal"));
        assertEquals(3532,order.getLong("total"));
        assertEquals(800,order.getLong("fee"));
        assertTrue(new LocalStore(context).cart.isEmpty());
        assertEquals(1,new LocalStore(context).orders.length());
    }
}
