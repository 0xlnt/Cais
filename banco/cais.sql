DROP DATABASE IF EXISTS cais;
CREATE DATABASE cais CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cais;

CREATE TABLE usuario (
  id_usuario   INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(100) NOT NULL,
  email        VARCHAR(120) NOT NULL UNIQUE,
  senha_hash   VARCHAR(255) NOT NULL,
  telefone     VARCHAR(20),
  tipo         ENUM('cliente','vendedor') NOT NULL,
  criado_em    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vendedor (
  id_vendedor      INT PRIMARY KEY,
  nome_loja        VARCHAR(100) NOT NULL,
  descricao        VARCHAR(255),
  bairro           VARCHAR(80),
  loja_aberta      BOOLEAN DEFAULT TRUE,
  aceita_retirada  BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (id_vendedor) REFERENCES usuario(id_usuario)
);

CREATE TABLE categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE zona_entrega (
  id_zona        INT AUTO_INCREMENT PRIMARY KEY,
  bairro         VARCHAR(80) NOT NULL UNIQUE,
  taxa           DECIMAL(8,2) NOT NULL,
  pedido_minimo  DECIMAL(8,2) DEFAULT 0
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
  data_pesca    DATE NOT NULL,
  disponivel    BOOLEAN DEFAULT TRUE,
  sob_encomenda BOOLEAN DEFAULT FALSE,
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
  id_endereco   INT NULL,
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
  preco_kg       DECIMAL(8,2) NOT NULL,
  FOREIGN KEY (id_pedido)  REFERENCES pedido(id_pedido),
  FOREIGN KEY (id_produto) REFERENCES produto(id_produto)
);

CREATE TABLE avaliacao (
  id_avaliacao  INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido     INT NOT NULL UNIQUE,
  nota_vendedor TINYINT NOT NULL CHECK (nota_vendedor BETWEEN 1 AND 5),
  nota_frescor  TINYINT NOT NULL CHECK (nota_frescor BETWEEN 1 AND 5),
  comentario    VARCHAR(255),
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
);

INSERT INTO categoria (nome) VALUES ('Peixe'), ('Camarão e frutos do mar');

INSERT INTO usuario (nome, email, senha_hash, tipo) VALUES
 ('Carlos Braga',  'carlos@cais.com',   'hash_teste', 'vendedor'),
 ('Maria Souza',   'maria@cais.com',    'hash_teste', 'vendedor'),
 ('Raimundo Lima', 'raimundo@cais.com', 'hash_teste', 'vendedor'),
 ('Cliente Demo',  'cliente@cais.com',  'hash_teste', 'cliente');

INSERT INTO vendedor (id_vendedor, nome_loja, bairro) VALUES
 (1, 'Peixaria Maré Boa',   'Centro'),
 (2, 'Viveiro da Maria',    'Cidade Nova'),
 (3, 'Pescado do Raimundo', 'Educandos');

INSERT INTO produto (id_vendedor, id_categoria, nome, preco_kg, data_pesca) VALUES
 (1, 1, 'Filé de tilápia fresco', 39.90, '2026-11-13'),
 (1, 1, 'Posta de salmão fresca', 54.90, '2026-11-13'),
 (2, 1, 'Tambaqui',               32.00, '2026-11-12'),
 (2, 2, 'Camarão cinza',          48.00, '2026-11-12'),
 (3, 1, 'Pirarucu',               45.00, '2026-11-11'),
 (3, 2, 'Camarão regional',       52.00, '2026-11-13');

INSERT INTO zona_entrega (bairro, taxa, pedido_minimo) VALUES
 ('Centro', 8.00, 0),
 ('Cidade Nova', 15.00, 40.00);