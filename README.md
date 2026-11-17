# Projetos CRUD em Java com CSV

Coleção de 6 sistemas de cadastro (CRUD) feitos em **Java** com interface gráfica em **Swing**, usando um arquivo **CSV** como banco de dados.

Todos os projetos seguem a mesma arquitetura, mudando apenas o contexto de cada cadastro.

## Projetos

| Projeto | Campos | Arquivo |
|---|---|---|
| `BooksCatalog` | Título, Autor, Ano de Publicação, Gênero | `books.csv` |
| `MoviesCatalog` | Título, Diretor, Ano de Lançamento, Gênero | `movies.csv` |
| `StudentsCatalog` | Nome, Idade, Curso, Matrícula | `students.csv` |
| `ProductsCatalog` | Nome do Produto, Preço, Categoria, Código | `products.csv` |
| `EmployeesCatalog` | Nome, Cargo, Salário, Data de Admissão | `employees.csv` |
| `EventsCatalog` | Nome do Evento, Local, Data, Organizador | `events.csv` |

## Funcionalidades

- Escolha da pasta do arquivo ao iniciar (o CSV é criado automaticamente se não existir)
- Listagem dos registros em uma tabela
- Adicionar, editar e apagar registros
- Todas as alterações são salvas no arquivo CSV na hora
- Validação dos campos no formulário

## Estrutura

Cada projeto é dividido em três camadas:

```
src/
├── Entities/   → classes do modelo (ex.: Book, Genre)
├── Services/
│   ├── ListService.java  → gerencia a lista em memória e chama o CsvServices
│   └── CsvServices.java  → lê e grava o arquivo CSV
└── GUI/
    └── MainWindow.java   → interface gráfica (Swing)
```

Fluxo: `MainWindow` → `ListService` → `CsvServices` → arquivo `.csv`
