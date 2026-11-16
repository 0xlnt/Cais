CREATE DATABASE IF NOT EXISTS cais CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cais;

-- Usuário base (login). O tipo separa cliente de vendedor.
CREATE TABLE usuario (
  id_usuario   INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(100) NOT NULL,
  email        VARCHAR(120) NOT NULL UNIQUE,
  senha_hash   VARCHAR(255) NOT NULL,          -- RNF07: nunca guardar senha pura
  telefone     VARCHAR(20),
  tipo         ENUM('cliente','vendedor') NOT NULL,
  criado_em    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendedor (
  id_vendedor      INT PRIMARY KEY,            -- mesmo id do usuário (1 para 1)
  nome_loja        VARCHAR(100) NOT NULL,
  descricao        VARCHAR(255),
  bairro           VARCHAR(80),
  loja_aberta      BOOLEAN DEFAULT TRUE,
  aceita_retirada  BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (id_vendedor) REFERENCES usuario(id_usuario)
);

CREATE TABLE categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(50) NOT NULL UNIQUE     -- peixe, camarão, frutos do mar
);

CREATE TABLE zona_entrega (
  id_zona        INT AUTO_INCREMENT PRIMARY KEY,
  bairro         VARCHAR(80) NOT NULL UNIQUE,
  taxa           DECIMAL(8,2) NOT NULL,
  pedido_minimo  DECIMAL(8,2) DEFAULT 0        -- RF07: só pesa em zonas distantes
);

CREATE TABLE endereco (
  id_endereco  INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente   INT NOT NULL,
  id_zona      INT NOT NULL,
  rua          VARCHAR(120) NOT NULL,
  numero       VARCHAR(10),
  complemento  VARCHAR(60),
  FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_zona)    REFERENCES zona_entrega(id_zona)
);

CREATE TABLE produto (
  id_produto    INT AUTO_INCREMENT PRIMARY KEY,
  id_vendedor   INT NOT NULL,
  id_categoria  INT NOT NULL,
  nome          VARCHAR(100) NOT NULL,
  descricao     VARCHAR(255),
  preco_kg      DECIMAL(8,2) NOT NULL,
  foto_url      VARCHAR(255),
  data_pesca    DATE NOT NULL,                 -- validação do frescor
  disponivel    BOOLEAN DEFAULT TRUE,          -- RF10
  sob_encomenda BOOLEAN DEFAULT FALSE,         -- RF12 (peixes exóticos)
  FOREIGN KEY (id_vendedor)  REFERENCES vendedor(id_vendedor),
  FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

CREATE TABLE defeso (
  id_defeso    INT AUTO_INCREMENT PRIMARY KEY,
  especie      VARCHAR(80) NOT NULL,
  data_inicio  DATE NOT NULL,
  data_fim     DATE NOT NULL,
  motivo       VARCHAR(255)
);

CREATE TABLE pedido (
  id_pedido     INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente    INT NOT NULL,
  id_vendedor   INT NOT NULL,
  id_endereco   INT NULL,                      -- NULL quando for retirada
  tipo_entrega  ENUM('entrega','retirada') NOT NULL,
  status        ENUM('recebido','em_preparo','saiu_para_entrega','pronto_retirada','entregue','recusado') DEFAULT 'recebido',
  subtotal      DECIMAL(10,2) NOT NULL,
  taxa_entrega  DECIMAL(8,2) DEFAULT 0,
  total         DECIMAL(10,2) NOT NULL,
  criado_em     DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_cliente)  REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_vendedor) REFERENCES vendedor(id_vendedor),
  FOREIGN KEY (id_endereco) REFERENCES endereco(id_endereco)
);

CREATE TABLE item_pedido (
  id_item        INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido      INT NOT NULL,
  id_produto     INT NOT NULL,
  quantidade_kg  DECIMAL(6,3) NOT NULL,
  preco_kg       DECIMAL(8,2) NOT NULL,        -- preço no momento da compra
  FOREIGN KEY (id_pedido)