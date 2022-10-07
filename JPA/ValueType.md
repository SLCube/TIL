# JPA 값 타입

## JPA 데이터 타입 분류
1. 엔티티 타입
    - @Entity로 정의하는 객체
2. 값 타입

---
## 값 타입 분류
- 기본값 타입
    - java primitive type
    - embedded type
    - collection value type
---

## Embedded type
- 새로운 값 타입을 직접 정의 할 수 있음.
- 보통 int, String과 같이 기본 값 타입을 모아 만들어서 만듦

```java
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String city;
    private String street;
    private String zipcode;

}
```
- 이러한 Entity가 있을 때 근무기간(endDate - startDate) 와 집주소(city, street, zipcode)는 연관성이 높은 속성이고 재사용할 가능성이 보인다. 이럴때 사용하자

```java
public class Member{
    @Id
    @GeneratedValue
    priavate Long id;
    private String name;
    @Embedded
    private Period workPeriod;
    @Embedded
    private Address homeAddress;
}

@Embeddable
public class Period {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

@Embeddable
public class Address{
    private String city;
    private String street;
    private String zipcode;
}
```
### 임베디드 타입 사용법
- 기본 생성자는 필수
- 값 타입을 정의하는 곳엔 @Embeddable
- 값 타입을 사용하는 곳엔 @Embedded

### 장점
- 재사용
- 높은 응집도
- 값 타입만의 의미있는 메소드 생성 가능
---
### 임베디드 타입과 테이블 매핑
- 임베디드 타입은 엔티티의 값일 뿐
- 임베디드 타입을 사용하던 아니던 테이블 매핑 결과는 같다.
- 잘 설계한 ORM application은 테이블 수 보다 class수가 더 많다.