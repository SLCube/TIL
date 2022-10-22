# QueryDSL

QueryDSL은 오픈소스 프로젝트로 JPQL을 무려 Java코드로 작성할 수 있도록 하는 라이브러리이다.

특히 동적쿼리를 작성할 때 장점이 부각된다.

---
## QueryDSL 시작하기

QueryDSL을 사용하기위해선 단순히 의존성추가만 해줘선 사용할 수 없다.

```groovy
buildscript {
	ext {
		queryDslVersion = "5.0.0"
	}
}

plugins {
	id 'org.springframework.boot' version '2.7.4'
	id 'io.spring.dependency-management' version '1.0.14.RELEASE'
	id 'java'
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
}

group = 'study'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.1'

	//querydsl 추가
	implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
	annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}"

	compileOnly 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}

// querydsl 세팅 시작
def querydslDir = "$buildDir/generated/querydsl"
querydsl {
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main.java.srcDir querydslDir
}
configurations {
	querydsl.extendsFrom compileClasspath
}
compileQuerydsl {
	options.annotationProcessorPath = configurations.querydsl
}
// querydsl 세팅 끝
```

위 세팅을 끝내고 gradle compileQuerydsl을 실행시키면 QueryDSL을 사용할 준비가 끝난상태이다.

문법을 나열하기보단 이렇게 사용할 수 있다 라는 간단한 코드를 적어보겠다.

```java
@Configuration
public class QuerydslConfig {

    private final EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
```

```java
public interface MemberQueryRepository {
    public List<Member> findMemberSearch(MemberSearchCondition condition);
}
```

```java
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {
    
    private final JPAQueryFactory query;

    @Override
    public List<Member> findMemberSearch(MemberSearchCondition condition) {
        return query
                .select(member)
                .from(member)
                .where(
                    usernameEq(condition.getUsername()), 
                    ageGoe(condition.getAgeGoe()), 
                    ageLoe(condition.getAgeLod())
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
```

```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryRepository {

}
```

인상깊은점은 QueryDSL의 문법을 다루지 않았음에도 SQL다루듯이 다룰수있다는 점이다.

특이한점은 where조건을 콤마로 구분해 조건을 나열하며 조건에 null이 들어가면 조건을 생성하지 않는다는 특징을 이용해 동적쿼리를 다루고있다.

Spring Data JPA에서 지원하는 사용자 정의 Repository를 이용해 구현했기 때문에 Service계층에서 사용할땐 MemberRepository를 이용해 사용하면 된다.

---

## JPA + Querydsl을 공부하면서..

나는 일할때 mybatis를 이용해 query를 다루고 있다. mybatis를 다루면서 어느순간 답답하다라는 생각이 들었다.

간단한 CRUD를 반복적으로 다루는 점. 그리고 SQL을 문자열로 다룬다는 것이었다.

실수로 오타를 내더라도 그 순간에 찾지 못하고 에러가 나면 어디서 에러가 났는지 찾아야되는 그 답답함..

어떤 프로젝트에선 쿼리에 모든 비즈니스로직을 담아 DTO로 담아 반환하고 자바는 그저 DTO를 운반하는 경로로 사용하는 프로젝트도 있었다.

이게 맞나..? OOP에대해 잘 알아요! 라고 말할 수 없지만, 아니 지금은 잘 모른다라고 할 수 있지만 그럼에도 이건 Java를 Java스럽지 못하게 다루고있다 라는것 정도는 알 수 있었다.  

우연히 JPA라는 기술이 있다는걸 알았고 JPA를 JPA답지 못하게 사용하는 코드도 봤다. JPA는 섬세한 쿼리를 다루기 어렵다고... 그게 내 JPA의 첫 인상이었다. 어렵기만하고 섬세하게 쿼리를 다루려면 문자열로 쿼리를 다루고 간단한 쿼리라도 조건이 여러개가 붙으면 method이름이 길어지고...

QueryDSL을 사용한 코드를 봤을땐 내가 뭔가 잘못알고있었구나 라고 깨달았고 공부하고 싶었다. 그리고 잘했다고 느꼈다. JPA + QueryDSL을 공부하면서 간단한 CRUD쿼리는 Spring Data JPA가 알아서 만들어주고 QueryDSL을 이용해 내가 원하는 동적쿼리를 섬세하게 다룰 수 있었다. 물론 정말 복잡한 쿼리들은 Native SQL을 사용해야겠지만... 

이젠 뭘 공부해야될까.. 위에서 적은것중에 Java를 Java답게 사용한다는것 난 지금 그 대답을 할 수 있을까? OOP를 OOP답게 사용한다는것 이제 그걸 공부해야겠다.