# 4장 클래스와 인터페이스

## item15 클래스와 멤버의 접근 권한을 최소화하라

잘 설계된 컴포넌트란? 클래스 내부 데이터와 내부 구현 정보를 외부 컴포넌트로부터 얼마나 잘 숨겼느냐이다. 잘 설계된 컴포넌트는 모든 내부 구현을 완벽히 숨겨, 구현과 외부 API를 깔끔하게 분리함. 컴포넌트끼리의 소통은 내부구현은 신경쓰지 않고 오직 API로만 소통한다. 이 개념은 정보 은닉 혹은 캡슐화 라고함.

정보 은닉의 장점
- 시스템 개발 속도를 높인다.
    - 각각의 컴포넌트들을 병렬적으로 개발할 수 있다.
- 시스템 관리 비용을 낮춘다.
- 성능 최적화에 도움을 준다.
- 소프트웨어 재사용성을 높인다.
- 큰 시스템 제작 난이도를 낮춰준다.

정보 은닉의 기본 원칙
 모든 클래스와 멤버의 접근성을 가능한 좁힌다.

class 와 interface에 부여할 수 있는 접근 수준은 public과 default. public으로 선언하면 공개 api, default로 선언하면 패키지내에서 사용하는 api. 패키지외부에서 쓸일이 없다면 default로 선언하자
public class의 인스턴스 필드는 되도록 public이 아니어야한다. 필드가 가변 객체를 참조하거나 final이 아니라면 스레드 안전하지 않다. 
---
## item16 public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라.

간단하게 말하자면 public class에선 private으로 선언하고 외부에 인스턴스 필드를 공개하지 말자.

```java
class Point {
    public double x;
    public double y;
}
```
이런 클래스는 외부에서 아무런 제약조건없이 필드에 접근할 수 있으며 수정도 가능하다.

```java
class Point {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
```
생각) 사실 setter에 대한 생각을 굉장히 많이했었다. 왜냐면 모든 필드에대해 setter를 열게되면 public으로 선언한 필드랑 크게 다를게 없다고 생각했기 때문이다. 차이점이라면 직접 필드에 접근하는것 vs 메소드를 통해 필드에 접근하는것 이정도이려나.. 개인적으로는 특정 필드가 변경이 필요하다면 setter보다는 유의미한 이름을 갖는 메소드를 제공하는편이 더 낫지않나 싶다.

예를들면..
```java
class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
```

```java
Person p = new Person("홍길동", 15);
p.changeName("홍철수");
```

어떤 사람이 개명을 한다고 생각하고 간단하게 코드를 짜봤다. 
위의 코드는 간단하기 때문에 setter에서 유의미한 이름을 갖는 메소드로 바뀌었다해서 크게 달라진점을 못느낄지 몰라도 복잡한 상황이라면 메소드의 코드를 직접 보지않더라도 그 메소드가 무슨일을 하는지 쉽게 파악할 수 있게 될것이다.

---
## item17 변경 가능성을 최소화하라

---
## item18 상속보다는 컴포지션을 사용하라

---
## item19 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

---
## item20 추상 클래스보다는 인터페이스를 우선하라

---
## item21 인터페이스는 구현하는 쪽을 생각해 설계하라

---
## item22 인터페이스는 타입을 정의하는 용도로만 사용하라

---
## item23 태그 달린 클래스보다는 클래스 계층구조를 활용하라

---
## item24 멤버 클래스는 되도록 static으로 만들라

---
## item25 톱레벨 클래스는 한 파일에 하나만 담으란