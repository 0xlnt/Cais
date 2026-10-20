package com.example.myapplication.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Rotas {
    const val LOGIN = "login"
    const val HOME = "home"
    const val BUSCA = "busca"
    const val PEDIDOS = "pedidos"
    const val PERFIL = "perfil"
    const val PRODUTO = "produto/{id}"
    const val CARRINHO = "carrinho"
    const val CHECKOUT = "checkout"
    const val PAINEL = "painel"
    const val NOVO_PRODUTO = "novoProduto"

    fun produto(id: Int) = "produto/$id"
}

private data class Aba(val rota: String, val rotulo: String, val icone: ImageVector)

private val abasCliente = listOf(
    Aba(Rotas.HOME, "Início", Icons.Filled.Home),
    Aba(Rotas.BUSCA, "Buscar", Icons.Filled.Search),
    Aba(Rotas.PEDIDOS, "Pedidos", Icons.Filled.Receipt),
    Aba(Rotas.PERFIL, "Perfil", Icons.Filled.Person)
)

private val abasVendedor = listOf(
    Aba(Rotas.PAINEL, "Pedidos", Icons.Filled.Receipt),
    Aba(Rotas.PERFIL, "Perfil", Icons.Filled.Person)
)

private val CoresCais = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Teal,
    background = Color.White,
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy
)

/** Vai para uma aba da barra inferior sem empilhar telas repetidas. */
private fun NavController.irParaAba(rota: String) {
    navigate(rota) {
        popUpTo(graph.id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun CaisApp(vm: CaisViewModel = viewModel()) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rota = entrada?.destination?.route
    val abas = if (vm.usuario?.ehVendedor == true) abasVendedor else abasCliente

    fun voltarParaLogin() {
        nav.navigate(Rotas.LOGIN) {
            popUpTo(nav.graph.id) { inclusive = true }
        }
    }

    MaterialTheme(colorScheme = CoresCais) {
        Scaffold(
            containerColor = Color.White,
            contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
            bottomBar = {
                if (rota != null && abas.any { it.rota == rota }) {
                    NavigationBar(containerColor = Color.White) {
                        abas.forEach { aba ->
                            NavigationBarItem(
                                selected = rota == aba.rota,
                                onClick = { nav.irParaAba(aba.rota) },
                                icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                                label = { Text(aba.rotulo) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Navy,
                                    selectedTextColor = Navy,
                                    indicatorColor = Ice,
                                    unselectedIconColor = Muted,
                                    unselectedTextColor = Muted
                                )
                            )
                        }
                    }
                }
            }
        ) { interno ->
            NavHost(
                navController = nav,
                startDestination = Rotas.LOGIN,
                modifier = Modifier.padding(interno)
            ) {
                composable(Rotas.LOGIN) {
                    LoginScreen(vm) { ehVendedor ->
                        nav.navigate(if (ehVendedor) Rotas.PAINEL else Rotas.HOME) {
                            popUpTo(Rotas.LOGIN) { inclusive = true }
                        }
                    }
                }

                composable(Rotas.HOME) {
                    HomeScreen(
                        vm = vm,
                        aoBuscar = { nav.irParaAba(Rotas.BUSCA) },
                        aoAbrirProduto = { nav.navigate(Rotas.produto(it)) },
                        aoCarrinho = { nav.navigate(Rotas.CARRINHO) }
                    )
                }

                composable(Rotas.BUSCA) {
                    BuscaScreen(
                        vm = vm,
                        aoAbrirProduto = { nav.navigate(Rotas.produto(it)) },
                        aoCarrinho = { nav.navigate(Rotas.CARRINHO) }
                    )
                }

                composable(
                    Rotas.PRODUTO,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entradaProduto ->
                    ProdutoScreen(
                        vm = vm,
                        produtoId = entradaProduto.arguments?.getInt("id") ?: 0,
                        aoVoltar = { nav.popBackStack() },
                        aoCarrinho = { nav.navigate(Rotas.CARRINHO) },
                        aoVerVendedor = { id ->
                            vm.limparFiltros()
                            vm.vendedorFiltro = id
                            nav.irParaAba(Rotas.BUSCA)
                        }
                    )
                }

                composable(Rotas.CARRINHO) {
                    CarrinhoScreen(
                        vm = vm,
                        aoVoltar = { nav.popBackStack() },
                        aoVerPeixes = { nav.irParaAba(Rotas.BUSCA) },
                        aoContinuar = { nav.navigate(Rotas.CHECKOUT) }
                    )
                }

                composable(Rotas.CHECKOUT) {
                    CheckoutScreen(
                        vm = vm,
                        aoVoltar = { nav.popBackStack() },
                        aoConfirmar = { nav.irParaAba(Rotas.PEDIDOS) }
                    )
                }

                composable(Rotas.PEDIDOS) {
                    PedidosScreen(vm = vm, aoVerPeixes = { nav.irParaAba(Rotas.BUSCA) })
                }

                composable(Rotas.PERFIL) {
                    PerfilScreen(vm = vm, aoSair = { voltarParaLogin() })
                }

                composable(Rotas.PAINEL) {
                    PainelVendedorScreen(
                        vm = vm,
                        aoNovoProduto = { nav.navigate(Rotas.NOVO_PRODUTO) }
                    )
                }

                composable(Rotas.NOVO_PRODUTO) {
                    NovoProdutoScreen(vm = vm, aoVoltar = { nav.popBackStack() })
                }
            }
        }
    }
}
