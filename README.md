# CaisApp

Aplicativo Android nativo em **Java**, com catálogo de pescados de Manaus e fluxo de compra local.

## Executar

1. Abra esta pasta no Android Studio e sincronize o Gradle.
2. Selecione um aparelho ou emulador com Android 7.0/API 24 ou superior.
3. Execute o módulo `app`.

O projeto mantém o SDK 37 e o Gradle fornecidos na configuração original. Java 25 executa o Gradle; o código do aplicativo usa compatibilidade Java 11.

Para gerar o APK:

```bash
./gradlew assembleDebug
```

Arquivo gerado: `app/build/outputs/apk/debug/app-debug.apk`.

## Funcionalidades

- Início com banners, produtos e vendedores do protótipo.
- Busca por produto, vendedor e loja, sem distinguir acentos ou maiúsculas.
- Categorias, promoções, favoritos e ordenação por preço/avaliação.
- Loja do vendedor, detalhes do produto e quantidade de 0,5 a 20 kg.
- Carrinho com cálculo em centavos, retirada gratuita e entrega de R$ 8,00.
- Checkout com endereço, observações e escolha de pagamento.
- Pedidos com filtros, detalhes, cancelamento, recompra e avanço de status simulado.
- Perfil local e mensagens com respostas automáticas de demonstração.
- Persistência de carrinho, favoritos, perfil, mensagens e pedidos ao fechar o app.

**É uma demonstração local:** os vendedores/produtos são dados de exemplo; pedidos e mensagens não são enviados a lojas. PIX/cartão não processam cobranças. Não há autenticação nem servidor. O endereço não é geolocalizado, e o avanço de entrega é simulado pelo usuário.

## Referências visuais

Figma: https://www.figma.com/design/xknQ7p9kQOX7oun5t6v1le/CaisApp-Prototipo?node-id=0-1

Foram lidas as telas `Home` (`31:3`) e `Search` (`45:392`, `57:182`). Detalhes do produto, checkout e pedidos seguem as imagens locais `ideiaPedido_escolhido.png`, `ideiaPagamento_e_Negociacao.png` e `ideiaPedidos.png`. Essas telas ainda podem ser ajustadas quando os desenhos finais estiverem prontos no Figma.

Os originais fornecidos continuam em `app/src/main/res/drawable/CaisApp-rsc-figma/`. Os arquivos usados pelo Android ficam em `drawable-nodpi`, com nomes compatíveis. `design/figma-assets/` guarda os recursos extraídos do Figma; `design/unused-android-assets/` guarda exportações não usadas na interface. A implementação usa layouts nativos com rolagem e alvos de toque ampliados, adaptando as medidas do protótipo.

## Código

- `MainActivity.java`: telas e navegação Android.
- `Catalog.java`: produtos, vendedores, busca e cálculo em centavos.
- `LocalStore.java`: estado local em SharedPreferences/JSON e criação dos pedidos.
- `activity_main.xml`: estrutura principal e navegação inferior.

Não há código Kotlin de aplicativo; os arquivos `.gradle.kts` são a configuração de build original.

## Verificação

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

Os testes cobrem busca, filtros, arredondamento de valores, persistência, frete, compra, recriação de tela e transições de pedido. Os testes instrumentados limpam **os dados de demonstração no dispositivo de teste**; execute-os em emulador dedicado.
