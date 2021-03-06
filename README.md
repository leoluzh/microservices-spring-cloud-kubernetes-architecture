# Arquitetura de Referência com Spring Cloud Kubernetes

Esta arquitetura de referência tem por objetivo demonstrar design, desenvolvimento e deployment de microservicos [`Spring Boot`](https://spring.io/projects/spring-boot) em [`Kubernetes`](https://kubernetes.io/).
Cada seção cobrirá recomendações arquiteturais e configurações para cada quando aplicável.

### Recomendações chaves de alto-nível

* Considerando melhores práticas para `aplicações nativas em nuvem` e os [`12 fatores`](https://12factor.net/pt_br/). 
* Mantenha cada microserviços em um projeto [`maven`](https://maven.apache.org/) ou [`gradle`](https://gradle.org/) separado.
* Prefira utilizar dependências ao herdar de um projeto pai no lugar de utilizar um caminho relativo.
* Utilize o [`Spring Initializr`](https://start.spring.io
  ) para poder gerar uma estrutura de projeto para Spring Boot.

Esta arquitetura demonstra um aplicativo nativo em nuvem complexo que abordará as seguintes questões:

* Configuração externalizada utilizando `ConfigMaps`, `Secrets` e `PropertySource`
* Acesso ao servidor da API Kubernetes utilizando `ServiceAccounts`, `Roles` e `RoleBidings`.
* Verificação de saúde/estado utilizando sondas
    * `readinessProbe`
    * `livenessProbe`
    * `startupProbe`
* Relatório deo estado do aplicativo por meio do Spring Boot [`Actuators`](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)    
* Descoberta de serviço por [`namespaces`](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/) utilizando `DiscoveryClient`
* Construção de imagem [`Docker`](https://www.docker.com/) utilizando boas práticas
* [`Layering JARs`](https://spring.io/blog/2020/08/14/creating-efficient-docker-images-with-spring-boot-2-3) utilizando o plugin do SpringBoot
* Observando a aplicação utilizando exportadores como [`Prometheus`](https://www.prometheus.io/)

## Arquitetura de Referência

A arquitetura de referência demonstra uma organização onde cada unidade tem seu próprio aplicativo projetado utilizando uma arquitetura de microserviços.
Os microserviços serão expostos através `APIs REST` utilizando Spring Boot em um servidor Tomcat integrado e implantado no Kubernetes.

Cada microserviço é implantado em seu próprio `namespace`. Colocar microserviços em namespaces distintos permite agrupamento lógico que facilita o gerenciamento de privilégios de acesso, atribuindo-lhes apenas uma equipe responsável por um determinado aplicativo. O `Spring Cloud Kubernetes Discovery Client` torna a comunicação interna entre microserviços perfeita. Ele se comunica com a API Kubernetes para descobrir os IPs de todos os serviços em execução nos `PODs`.

O aplicativo implementa nesta Arquitetura de Referência é construído com vários componentes de código aberto, comumente encontrados na maioria das implantações de microserviços Spring Boot. Esse incluem:

* [`Spring Cloud Kubernetes`](https://spring.io/projects/spring-cloud-kubernetes): fornece integração com servidor de API Kubernetes para permitir a descoberta de serviço, configuração e balanceamento de carga utilizando pelo Spring Cloud.
* [`Spring Cloud`](https://spring.io/projects/spring-cloud): fornece ferramentas para que os desenvolvedores criem rapidamente padrões comuns em sistemas distruídos.
    * [`OpenFeign`](https://spring.io/projects/spring-cloud-openfeign) - Binder de cliente Java para HTTP
    * [`Zuul`](https://spring.io/projects/spring-cloud-gateway) - solicitações de roteamento e filtragem para um microserviço.
    * [`Sleuth`](https://spring.io/projects/spring-cloud-sleuth) - um ferramenta para rastreamento distribuído
    * `Swagger` - conjunto de ferramentas de código aberto construído em torno da especificação OpenAPI que pode projetar, construir, documentar e consumir APIs REST.
    * `UI Swagger` - fornece iteração e visualização de recursos de API sem escrever lógica customizada.

![Arquitetura de Referência](./resources/architecture.png)

## Ambiente da Arquitetura de Referência

Cada microserviços é executado em seu próprio contêiner, um contêiner por pod e um pod por replica de serviço. O aplicativo é contruído utilizando um arquitetura de microserviços e representado por contêineres replicados que chamam uns aos outros.

![Arquitetura de Referência com Contêiners e Pods](./resources/architecture-microservice-pod.png)

## Spring Cloud Kubernetes

Spring Cloud Kubernetes fornece implantações do Spring Cloud de interfaces comuns que consomem serviços nativos do Kubernetes. Seu objetivo é facilitar a integração dos aplicativos Spring Cloud e Spring Boot em execução no Kubernetes.

Spring Cloud Kubernetes integra-se à API Kubernetes e permite a descoberta, configuração e balanceamento de carge do serviço. Essa arquitetura de referência demonstra o uso dos seguintes recursos do Spring Cloud Kubernetes:

* Descobrir serviços em todos os namespaces utilizando o Spring Cloud DiscoveryClient.
* Utilizar ConfigMap e Secrets como fontes de propriedades do Srping Boot com Spring Cloud Kubernetes Config.
* Implementar verificações de integridade utilizando o indicador de integridade do pod do Spring Cloud Kubernetes.

## Ativando Spring Cloud Kubernetes

Adicione a seguinte dependência para ativar os recursos do Spring Cloud Kubernetes no projeto.
A biblioteca contém módulos para descoberta de serviços, configuração e balanceamento de carga (Ribbon).

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-kubernetes-all</artifactId>
</dependency>
```

## Habilitando Service Discovery em todos os namespaces

Spring Cloud Kubernetes permite a descoberta de serviço do Kubernetes com aplicativos Spring Boot, fornecendo um implementação de Discovery Client. O cliente (Ribbon) se comunica diretamente com os pods, ao invés de comunicar-se por meio dos serviços kubernetes.
Isso permite um balanceamento de carga mais completo em oposição ao `round robin, que é forçado ao passar pelos serviços Kubernetes.
Ribbon é utilizando por cliente HTTP de alto-nível [OpenFeign](https://cloud.spring.io/spring-cloud-openfeign/reference/html).
MongoBD será utilizado para o repositório de dados. Swagger2 é um projeto de código aberto utilizado para gerar a documentação REST API para RESTfult web services.

```file
/spring-microservices-k8s/department-service/src/main/java/vmware/services/department/DepartmentApplication.java
```

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableMongoRepositories
@EnableSwagger2
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = RibbonConfiguration.class)
public class DepartmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DepartmentApplication.class, args);
    }

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("vmware.services.department.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(new ApiInfoBuilder().version("1.0").title("Department API").description("Documentation Department API v1.0").build());
    }
}
```

Para habilitar a descoberta em todos os namespaces, a propriedade `all-namespaces` precisa ser definida com `true`.

```file
/spring-microservices-k8s/k8s/department-configmap.yaml
```

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: department
data:
  spring.cloud.kubernetes.discovery.all-namespaces: "true"
  # other config removed for brevity.
```

Feign é um cliente de serviço web declarativo. Para comunicar-se com `employee-service` em `department-service` precisamos criar uma interface e utilizar a anotação `@FeignClient`.
Na anotação `@FeignClient`, o valor da string `"employee"` é um nome arbitrário para o cliente, que é utilizado para criar/mapear um balanceador de carga no Ribbon. 
O nome do bean no contexto do aplicativo é um nome totalmente qualificado interface. Em tempo execução, `employee`será resolvido com uma pesquisa no servidor de descoberta para um endereço IP de `employee` no serviço kubernetes.

```file
/spring-microservices-k8s/department-service/src/main/java/vmware/services/department/client/EmployeeClient.java
```

```java
@FeignClient(name = "employee")
public interface EmployeeClient {

    @GetMapping("/department/{departmentId}")
    List<Employee> findByDepartment(@PathVariable("departmentId") String departmentId);

}
```

## Criando namespaces do Kubernetes

Crie namespaces do Kubernetes para cada microserviço.

```script
kubectl create namespace department
kubectl create namespace employee
kubectl create namespace gateway
kubectl create namespace organization
kubectl create namespace mongo
```

## Configurando Spring Cloud Kubernetes para acessar a API Kubernetes

Spring Cloud Kubernetes requer acesso à API Kubernetes para poder recuperar a lista de endereços IP dos pods que serão liderados por um único serviço. A maneira mais simples de fazer isso é definir uma lista `ClusterRole`com recursos e verbos. 

```file
/spring-microservices-k8s/k8s/rbac-cluster-role.yaml
```

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: default
  name: microservices-kubernetes-namespace-reader
rules:
  - apiGroups: [""] # "" indicates the core API group
    resources: ["configmaps", "pods", "services", "endpoints", "secrets"]
    verbs: ["get", "list", "watch"]
```
Em seguida, crie-o no `default` namespaces.

```script
kubectl apply -f ../k8s/rbac-cluster-role.yaml -n default
```

Crie contas de serviço para `api-service-account`

```script
kubectl create serviceaccount api-service-account -n department
kubectl create serviceaccount api-service-account -n employee
kubectl create serviceaccount api-service-account -n gateway
kubectl create serviceaccount api-service-account -n organization
kubectl create serviceaccount api-service-account -n mongo
```

vincule contas de serviço `api-service-account` de cada namespace a `ClusterRole`

```script
kubectl create clusterrolebinding service-pod-reader-department --clusterrole=microservices-kubernetes-namespace-reader --serviceaccount=department:api-service-account
kubectl create clusterrolebinding service-pod-reader-employee --clusterrole=microservices-kubernetes-namespace-reader --serviceaccount=employee:api-service-account
kubectl create clusterrolebinding service-pod-reader-gateway --clusterrole=microservices-kubernetes-namespace-reader --serviceaccount=gateway:api-service-account
kubectl create clusterrolebinding service-pod-reader-organization --clusterrole=microservices-kubernetes-namespace-reader --serviceaccount=organization:api-service-account
kubectl create clusterrolebinding service-pod-reader-mongo --clusterrole=microservices-kubernetes-namespace-reader --serviceaccount=mongo:api-service-account
```

e certifique-se de que os manifestos de implantações para cada conta de serviço de referência de microserviços `api-service-account`

* `/spring-microservices-k8s/k8s/department-deployment.yaml`

* `/spring-microservices-k8s/k8s/employee-deployment.yaml`

* `/spring-microservices-k8s/k8s/organization-deployment.yaml`


```
# other config removed for brevity.
serviceAccountName: api-service-account
```

## Nomenclatura de serviço kubernetes

Cada serviço definido no cluster (incluindo o próprio servidor DNS) recebe um nome DNS. Os serviços ''normais'' (not headless) são atribuídos a um registro DNS A ou AAAA, dependendo da família de IP do serviço, para um nome na forma `my-svc.my-namespace.svc.cluster-domain.example`. Isso é resolvido para o IP do cluster do serviço. 

Os registros DNS para cada serviços são os seguintes:

![Arquitetura de Referência - registros DNS para serviços](./resources/architecture-dns.png)

## Configurando MongoBD

### Implementando MongoBD

A implementação do MongoBD é instruída a ler um ConfigMap para obter `database-name` e ler o secrets para obter `database-username` e `database-password`.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongodb
  labels:
    app: mongodb
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
        - name: mongodb
          image: mongo:4.2.3
          ports:
            - containerPort: 27017
          env:
            - name: MONGO_INITDB_DATABASE
              valueFrom:
                configMapKeyRef:
                  name: mongodb
                  key: database-name
            - name: MONGO_INITDB_ROOT_USERNAME
              valueFrom:
                secretKeyRef:
                  name: mongodb
                  key: database-user
            - name: MONGO_INITDB_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mongodb
                  key: database-password
          resources:
            requests:
              cpu: "0.2"
              memory: 300Mi
            limits:
              cpu: "1.0"
              memory: 300Mi
          readinessProbe:
            tcpSocket:
              port: 27017
            initialDelaySeconds: 50
            timeoutSeconds: 2
            periodSeconds: 20
            failureThreshold: 5
          livenessProbe:
            tcpSocket:
              port: 27017
            initialDelaySeconds: 50
            timeoutSeconds: 2
            periodSeconds: 20
            failureThreshold: 5
    serviceAccountName: api-service-account
```

### MongoBD ConfigMap

Em um ConfigMap, o nome do banco de dados mongo é definido em `database-name`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mongodb
data:
  database-name: admin
```

### Secrets do MongoBD

O nome de usuário e senha são definidos em `database-username` e `database-passoword` respectivamente. Como esté um objeto secreto, os valores devem ser codificados em base64.

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mongodb
type: Opaque
data:
  database-user: bW9uZ28tYWRtaW4=
  database-password: bW9uZ28tYWRtaW4tcGFzc3dvcmQ=
```

## Utilizando Spring Cloud Kubernete ConfigMap PropertySource

O recurso PropertySource do Spring Cloud Kubernetes permite o consumo de objetos ConfigMap e Secret diretamente no aplicativo, sem injetá-los em uma implantação. O comportamento padrão é baseado em `metadata.name` ConfigMap ou Secret, que deve ser igual ao nome de um aplicativo definido pela propriedade `spring.aplication.name`.
Por examplo, o ``department-service`` decompões-se da seguinte forma

* `/spring-microservices-k8s/department-service/src/resources/bootstrap.yml`

```yaml
spring:
  application:
    name: department
# other config removed for brevity.
```

* `/spring-microservices-k8s/k8s/department-configmap.yaml`

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: department
data:
  logging.pattern.console: "%clr(%d{yy-MM-dd E HH:mm:ss.SSS}){blue} %clr(%-5p) %clr(${PID}){faint} %clr(---){faint} %clr([%8.15t]){cyan} %clr(%-40.40logger{0}){blue} %clr(:){red} %clr(%m){faint}%n"
  spring.cloud.kubernetes.discovery.all-namespaces: "true"
  spring.data.mongodb.database: "admin"
  spring.data.mongodb.host: "mongodb.mongo.svc.cluster.local"
  spring.output.ansi.enabled: "ALWAYS"
```

* `data.logging.pattern.console`: define o padrão de registro
* `data.spring.cloud.kubernetes.discovery.all-namespaces : permite a descoberta de vários namespaces
* `spring.data.mongodb.database`: Nome do Mongo DB
* `spring.data.mongodb.host`: Localização do Mongo DB
* `spring.output.ansi.enabled`: Aplicar saída ANSI

## Utilizando Secrets por meio de volumes montados

Kubernetes tem noção de [secrets](https://kubernetes.io/docs/concepts/configuration/secret/) para armazenar dados confidenciais, como senhas, tokens Oath, chaves, etc.

Embora existam várias maneiras de compartilhar segredos com um contêiner, é recomendado compartilhar segredos por meio de volumes montados. 
Se você habilitar o consumo de segredos por meio da API, recomendamos limitar o acesso com políticas de autorização [RBAC](https://kubernetes.io/docs/concepts/configuration/secret/#best-practices) .

NOTA: os segredos não são seguros, são apenas uma ofuscação


Neste exemplo, um segredo chamado `department`é montado para o arquivo `/etc/secretspot` via de volume `mongodb`para as seguintes microservices: `department-service`, `employee-service`, `organization-service`.

```file
/spring-microservices-k8s/department-service/src/main/resources/bootstrap.yaml
```

```yaml
spring:
application:
  name: department
cloud:
  kubernetes:
    secrets:
      enabled: true
      paths:
        - /etc/secretspot
      enableApi: false
      # other config removed for brevity.
```

```file
/spring-microservices-k8s/k8s/department-deployment.yaml
```
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: department
  labels:
    app: department
spec:
  replicas: 1
  selector:
    matchLabels:
      app: department
  template:
    metadata:
      labels:
        app: department
    spec:
      containers:
        volumeMounts:
          - name: mongodb
            mountPath: /etc/secretspot
      volumes:
        - name: mongodb
          secret:
            secretName: department
      # other config removed for brevity.
```

As credenciais do Mongo são definidas dentro do objeto secreto 

```file
/spring-microservices-k8s/k8s/department-secret.yaml
```

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: department
type: Opaque
data:
  spring.data.mongodb.username: bW9uZ28tYWRtaW4=
  spring.data.mongodb.password: bW9uZ28tYWRtaW4tcGFzc3dvcmQ=
```

## Utilizando Spring Boot Actuator para exportar métricas para Prometheus

[Prometheus](https://prometheus.io/) é um sistema de monitoramento de código aberto. Spring Boot usa [Micrometer](http://micrometer.io/) , uma fachada de métricas de aplicativo para integrar métricas [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready) com sistemas de monitoramento externos. Ele suporta vários sistemas de monitoramento como Prometheus, Netflix Atlas, AWS CloudWatch, Datadog, InfluxData, SignalFx, Graphite, Wavefront, etc. O [spring-boot-starter-actuator](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-actuator) módulo fornece todos os [recursos prontos para produção](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#production-ready-endpoints) do [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#production-ready-endpoints).

Para integrar o Atuador com o Prometheus, você precisa adicionar as seguintes dependências no projeto maven para cada microsserviço:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

configure `metrics` e `prometheus` endpoints em configMap para cada projeto ( `department-configmap.yaml`, `employee-configmap.yaml`, `organization-configmap.yaml`):

```yaml
kind: ConfigMap
apiVersion: v1
# removed for brevity
data:
  # removed for brevity
  management.endpoints.web.exposure.include: "health,info,metrics,prometheus"
  management.metrics.enable.all: "true"
```

e adicione `MeterRegistryCustomizer` para Grafana [Spring Boot 2.1 Statistics](https://grafana.com/grafana/dashboards/10280) dashboard nas seguintes classes `@SpringBootApplication`: `DepartmentApplication`, `EmployeeApplication`, `GatewayApplication`, `OrganizationApplication` certifique-se de configurar .commonTags("application", "<microservice_name>") com nome atual do microserviço:

```java
import io.micrometer.core.instrument.MeterRegistry;
// removed for brevity
public class DepartmentApplication {
  // removed for brevity
  @Bean
  MeterRegistryCustomizer meterRegistryCustomizer(MeterRegistry meterRegistry){
    return registry -> {
      meterRegistry.config().commonTags("application", "department");
    };
  }
}
```

Os endpoints expostos estão disponíveis em `http://<host>:<port>/actuator/metrics` e `http://<host>:<port>/actuator/prometheus` com dados de métricas formatados especificamente para o Prometheus.

## Construindo aplicações com Spring Cloud Kubernetes

Construção de `department-service`

A partir do [Spring Boot 2.3.1.RELEASE](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#repackage-layers) , o Spring Boot pode ajudá-lo a empacotar os aplicativos Spring Boot em imagens Docker com Layered Jars.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
        <layers>
          <enabled>true</enabled>
        </layers>
      </configuration>
    </plugin>
  </plugins>
</build>
```

Depois de construir o projeto com JARs em camadas usando `jarmode` suporte, instale o pacote usando Maven.

```shell zsh
cd /spring-microservices-k8s/department-service/
mvn clean install
```

O JAR resultante pode ser testado para verificar se as camadas foram adicionadas.

```shell zsh
cd /spring-microservices-k8s/department-service/
java -Djarmode=layertools -jar target/department-service-1.1.jar list
```

Os comandos acima produzirão a seguinte saída e criarão os seguintes diretórios. eles devem ser adicionados:

* dependências
* dependências instantâneas
* spring-boot-loader
* aplicativo

### Construindo imagens Docker para aplicativos Spring

Para criar imagens compatíveis com as melhores práticas, `Dockerfile` deve-se considerar o seguinte.

* Removendo dependências de compilação do tempo de execução
  * por meio de compilações de vários estágios
* Colocando dependências (JARs) em sua própria camada
* Camada do aplicativo JAR
  * Garantir que os envios de imagens contenham apenas arquivos alterados
* Executando como não root e usando uma imagem de tempo de execução mínimo
  * via uso da imagem distroless

```dockerfile
ARG MVN_VERSION=3.6.3
ARG JDK_VERSION=11

FROM maven:${MVN_VERSION}-jdk-${JDK_VERSION}-slim as build

WORKDIR /build
COPY pom.xml .
# creates layer with maven dependencies
# first build will be significantly slower than subsequent
RUN mvn dependency:go-offline

COPY ./pom.xml /tmp/
COPY ./src /tmp/src/
WORKDIR /tmp/
# build the project
RUN mvn clean package

# extract JAR Layers
WORKDIR /tmp/target
RUN java -Djarmode=layertools -jar *.jar extract

# runtime image
FROM gcr.io/distroless/java:${JDK_VERSION} as runtime

USER nonroot:nonroot
WORKDIR /application

# copy layers from build image to runtime image as nonroot user
COPY --from=build --chown=nonroot:nonroot /tmp/target/dependencies/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/snapshot-dependencies/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/spring-boot-loader/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/application/ ./

EXPOSE 8080

ENV _JAVA_OPTIONS "-XX:MinRAMPercentage=60.0 -XX:MaxRAMPercentage=90.0 \
-Djava.security.egd=file:/dev/./urandom \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 \
-Dspring.output.ansi.enabled=ALWAYS \
-Dspring.profiles.active=default"

# set entrypoint to layered Spring Boot application
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

Construir a imagem Docker para o `department-service` aplicativo

```shell
cd /spring-microservices-k8s/department-service/
docker build -t vmware/department:1.1 .
```

e construir outros microservices: `employee-service`, `gateway-service`, `organization-service`.

Listar imagens do Docker

```shell
docker images
```

A ferramenta [Dive](https://github.com/wagoodman/dive) pode ser usada para explorar e analisar imagens e camadas do Docker. O abaixo exibe a buildimage. O Docker `Image Id` pode ser usado para explorar detalhes da imagem.

## Implamentando uma aplicação Spring Boot

Criamos namespace `department`, atribuímos `cluster-admin` e contruímos imagens `Docker` nas etapas anteriores

`department-service` é um manifesto de implantação do Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: department
  labels:
    app: department
spec:
  replicas: 1
  selector:
    matchLabels:
      app: department
  template:
    metadata:
      labels:
        app: department
    spec:
      containers:
        - name: department
          image: vmware/department:1.1
          ports:
            - containerPort: 8080
          resources:
            requests:
              cpu: "0.2"
              memory: 300Mi
            limits:
              cpu: "1.0"
              memory: 300Mi
          readinessProbe:
            httpGet:
              port: 8080
              path: /actuator/health
            initialDelaySeconds: 60
            timeoutSeconds: 2
            periodSeconds: 20
            failureThreshold: 5
          livenessProbe:
            httpGet:
              port: 8080
              path: /actuator/info
            initialDelaySeconds: 60
            timeoutSeconds: 2
            periodSeconds: 20
            failureThreshold: 5
      serviceAccountName: api-service-account
```
As cargas de trabalho devem sempre explicitamente especificadas

`spec.template.spec.containers.resources.requests` e 

`spec.template.spec.containers.resources.limits` e manter 

`spec.template.spec.containers.resources.requests.memory` e

`spec.template.spec.containers.resources.limits.memory` iguais , pois a RAM não é um recurso compactável como a CPU.

### Recomendações para sondas (probes) de aplicação:

 * `readinessProbe`: Sempre, torna o aplicativo disponível para tráfego após a aprovação das verificações.
 * `livenessProbe`: Usualmente, só deve verificar as condições em que reiniciar o aplicativo é uma solução apropriada. Certifique-se de não vincular essa verificação a dependências externas, como conectividade de banco de dados, ou poderá produzir falhas em cascata. Uma boa verificação pode ser atingir um endpoint, alocar alguma memória (por exemplo, matriz de bytes) e retornar um código `200`.
 * `startupProbe`: Raramente, as sondagens de prontidão e atividade (com um atraso) podem normalmente resolver o caso de uso que startupProbe tenta resolver. Se uma verificação específica for apropriada para um aplicativo que tem uma inicialização lenta a verificação nunca deve ser executada novamente, um `startupProbe` pode ser justificado.

O kubernetes aproveita as sondagens (probes/actuators) para determinar se o aplicativo está pronto para aceitar tráfego e se o aplicativo está ativo.

* Se `readinessProbe` não retornar código `200` - nenhum tráfego será roteado para ele. 
* Se `livenessProbe` não retornar código `200` - o kubernetes reiniciará o pod.

Spring boot tem um conjunto integrado de endpoints do módulo [`Actuator`](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

* `/actuator/health` : fornece informações básicas de integridade do aplicativo
* `/actuator/info` : fornece informações arbitrárias do aplicativo

Para adicionar o `Actuator`a um projeto baseado em Maven, adicione a seguinte dependência `Starter`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```
Para implantar `department-service` no kubernetes, execute o seguinte comando.

```shell
kubectl apply -n department -f ./spring-microservices-k8s/k8s/department-configmap.yaml
```

[`Octant`](https://github.com/vmware-tanzu/octant) é uma ótima ferramenta de código aberto que pode visualizar graficamente dependências de objetos kubernetes, encaminhar portas locais para um pod em execução, inspecionar logs de pod, navegar por diferentes clusters e ajudar os desenvolvedores a entender como os aplicativos são executados em um ambiente kubernete.

Visão geral do namespace `department` 

![Octant namespace department](./resources/octant-namespace-department.png)

Recursos associados ao pod implantado no namespace `department`

![Octant namepace department deployment](./resources/octante-namespace-department-deployment.png)


## Expondo um serviço

Utilizaremos a porta `8080` e tipo `NodePort` para expor todos os microserviços. A seguinte definição de serviço agrupa todos os pods rotulados com o campo `app` igual a `department`.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: department
  labels:
    app: department
spec:
  ports:
    - port: 8080
      protocol: TCP
  selector:
    app: department
  type: NodePort
```

## Configurando serviço de gateway

Utilizaremos o serviço `gateway-service` para expor a documentação do Swagger gerada automáticamente por meio da interface wbe Swagger UI para todos os microserviços distruídos entre os diversos namespaces. Para fazer isso, utilizaremos o `Spring Cloud Netflix Zuul`, que se integra à descoberta do kubernetes por meio do cliente `Ribbon`. 

Dependências necessárias:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-kubernetes-all</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger-ui</artifactId>
        <version>2.9.2</version>
    </dependency>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>2.9.2</version>
    </dependency>
</dependencies>
```

As configurações de rotas é direta e utiliza o Spring Cloud Kubernetes Discovery

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: gateway
data:
  logging.pattern.console: "%d{HH:mm:ss} ${LOG_LEVEL_PATTERN:-%5p} %m%n"
  spring.cloud.kubernetes.discovery.all-namespaces: "true"
  zuul.routes.department.path: "/department/**"
  zuul.routes.employee.path: "/employee/**"
  zuul.routes.organization.path: "/organization/**"
```

Como o proxy `Zuul` é integrado automaticamente ao `DiscoveryClient`, é fácil configurar resolução dinâmica de endpoints de microserviços pelo Swagger.

```java
@Configuration
public class GatewayApi {

    @Autowired
    ZuulProperties properties;

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>();
            properties.getRoutes().values().stream()
                    .forEach(route -> resources.add(createResource(route.getId(), "2.0")));
            return resources;
        };
    }

    private SwaggerResource createResource(String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(location);
        swaggerResource.setLocation("/" + location + "/v2/api-docs");
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }

}
```

Configure o client Ribbon para permitir a descoberta em todos os namespaces.

```java
public class RibbonConfiguration {

    @Autowired
    private DiscoveryClient discoveryClient;

    private String serviceId = "client";
    protected static final String VALUE_NOT_SET = "__not__set__";
    protected static final String DEFAULT_NAMESPACE = "ribbon";

    public RibbonConfiguration () {
    }

    public RibbonConfiguration (String serviceId) {
        this.serviceId = serviceId;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config) {

        Server[] servers = discoveryClient.getInstances(config.getClientName()).stream()
                .map(i -> new Server(i.getHost(), i.getPort()))
                .toArray(Server[]::new);

        return new StaticServerList(servers);
    }

}
```

A configuração de faixa de opções deve ser aplicada na classe principal do aplicativo:

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableSwagger2
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = RibbonConfiguration.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
```

## UI do Gateway Swagger

A UI Swagger é exposta no serviço de gateway e mostra uma vantagem de namespace cruzado `employee-service`, onde, `department-service` e `organization-service` estão disponíveis via `gateway-service`.

![Gateway Swagger](./resources/gateway-swagger.png)

## Configurando o Ingress

Kubernetes Ingress é um conjunto de regras que permiter que as solicitações recebidas cheguema aos serviços downstream. Ele funcione quase como um serviço com tipo `LoadBalancer`, mas você pode definir regras de roteamento personalizadas. O manifesto YAML precisa ser definido `host` como um nome de host sob o qual o gateway estará disponível. Em nossa arquitetura de microserviços, o Ingress tem a função de um gateway de API.

![Kubernetes Ingress](./resources/kubernetes-ingress.png)

```yaml
apiVersion: networking.k8s.io/v1beta1
kind: Ingress
metadata:
  name: gateway-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  backend:
    serviceName: default-http-backend
    servicePort: 80
  rules:
    - host: microservices-cluster.info
      http:
        paths:
          - path: /employee
            backend:
              serviceName: employee
              servicePort: 8080
          - path: /department
            backend:
              serviceName: department
              servicePort: 8080
          - path: /organization
            backend:
              serviceName: organization
              servicePort: 8080
```

Execute os seguintes comandos para aplicar a configuração acima no cluster Kubernetes

```shell
cd /spring-microservices-k8s/k8s/
kubectl apply -f ingress.yaml
```

## Teste de entrada

Execute os seguintes comando para preencher o MongoDB com dados de Teste

```shell
cd /spring-microservices-k8s/scrips/
./populate-data.sh
```

Execute o comando abaixo para verificar se `department-service` está disponível via `Ingress`

```shell
curl http://microservices-cluster.info/department/1/with-employees | jq
```

Exemplo de saída de `department-service`

```json
{
  "address": "Main Street",
  "id": "1",
  "name": "MegaCorp"
  "departments": [
    {
      "employees": [
        {
          "age": 25,
          "id": 1,
          "name": "Smith",
          "position": "engineer"
        },
        {
        "age": 45,
        "id": 2,
        "name": "Johns",
        "position": "manager"
        }
      ],
      "id": 1,
      "name": "RD Dept."
    }
  ],
  "employees": [
    {
      "age": 25,
      "id": 1,
      "name": "Smith",
      "position": "engineer"
    },
    {
      "age": 45,
      "id": 2,
      "name": "Johns",
      "position": "manager"
    }
  ],
}
```