<h2>Informações sobre o desafio</h2>

O desafio foi realizado na linguagem de programação JAVA (Java 11), usando o Spring Boot e o PostgreSQL como Banco de dados



<h2>Versão do JAVA</h2>

openjdk version "11.0.16.1" 2022-08-12 LTS



<h2>IDE utilizado</h2>

Spring Tool Suite 4



<h2>application.properties</h2>

Eu fiz algumas configurações para poder executar o projeto, como:

spring.datasource.url=jdbc:postgresql://localhost:5433/desafio
- Localmente a porta padrão pela qual o PostgreSQL está acessível é: 5432, mas estou usando a porta 5433 neste projeto.
- O nome do banco de dados que criei é: desafio


spring.datasource.username=postgres
- Este é o nome de usuário do banco de dados


spring.datasource.password=19handyrio
- Aqui está a senha que eu utilizei para acessar o banco de dados


server.port = 8090
- Aqui está a porta onde será executada o Spring Boot. O padrão é 8080, mas utilizei a porta 8090



<h2>Requisições</h2>

As requisições GET, POST, PUT e DELETE foram feitas utilizando o INSOMNIA 


Aqui, vou colocar a rota para realizar todas as requisições:


1) Adicionar uma pessoa (post/pessoas)
Link para acessar o endpoint: <a>http://localhost:8090/pessoas</a>
Modelo de JSON para testar: 
{
    "nome": "Joaquim",
    "departamento": {
        "id": 1
    }
}


2) Alterar uma pessoa (put/pessoas/{id})
Link para acessar o endpoint: <a>http://localhost:8090/pessoas/put/pessoas/6</a>
Modelo de JSON para testar:
{
    "nome": "Alex"
}


3) Remover uma pessoa (delete/pessoas/{id})
Link para acessar o endpoint: <a>http://localhost:8090/pessoas/delete/pessoas/6</a>


4) Adicionar uma tarefa (post/tarefas)
Link para acessar o endpoint: <a>http://localhost:8090/tarefas</a>
Modelo de JSON para testar:
{
	"titulo": "Validar NF Fevereiro",
	"descricao": "Validar notas recebidas no mês de Fevereiro",
	"prazo": "2022-03-15",
	"departamento": {
		"id": 1
	}
}


5) Alocar uma pessoa na tarefa que tenha o mesmo departamento (put/tarefas/alocar/{id})
Link para acessar o endpoint: <a>http://localhost:8090/tarefas/alocar/20/12</a>  (no final do endpoint temos em ordem o id da tarefa e o id da pessoa)


6) Finalizar a tarefa (put/tarefas/finalizar/{id})
Link para acessar o endpoint: <a>http://localhost:8090/tarefas/finalizar/20</a>


7) Listar pessoas trazendo nome, departamento, total horas gastas nas tarefas.(get/pessoas)
Link para acessar o endpoint: <a>http://localhost:8090/pessoas/getAllPessoa</a>


8) Buscar pessoas por nome e período, retorna média de horas gastas por tarefa.(get/pessoas/gastos)
Link testado na aplicação para acessar o endpoint: <a>http://localhost:8090/pessoas/gastos?nome=Joaquim&dataCriacao=2023-11-24T19:50:15.263905&duracao=4</a>


9) Listar 3 tarefas que estejam sem pessoa alocada com os prazos mais antigos. (get/tarefas/pendentes)
Link para acessar o endpoint: <a>http://localhost:8090/tarefas/pendentes</a>


10) Listar departamento e quantidade de pessoas e tarefas (get/departamentos)
Link para acessar o endpoint: <a>http://localhost:8090/departamentos</a>