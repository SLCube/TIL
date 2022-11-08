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

불변클래스 : 인스턴스 내부 값을 수정할 수 없는 클래스이다.

클래스를 불변으로 만들기 위한 다섯가지 원칙이다.
- Setter를 제공하지 않는다.
- 클래스를 확장할 수 없도록 만든다.
    - 해당 클래스를 final로 선언한다.
    - 모든 생성자를 private 혹은 default로 만들고 static 팩토리 메소드를 제공한다.
- 모든 필드를 final로 선언한다.
- 모든 필드를 private으로 선언한다.
- 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 만든다.
    - 클래스에서 가변 객체를 참조하는 필드가 하나라도 있으면 클라이언트에서 그 객체 참조를 얻을 수 없도록 해야된다.


```java
// 클래스를 확장할 수 없도록 final 클래스로 선언했다.
public final class Complex {

    // 모든 필드를 private과 final로 선언했다.
    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    // getter 성격을 갖는 realPart, imaginaryPart 메소드는 제공하지만 변경자 메소드는 제공하지 않는다.
    public double realPart() {
        return re;
    }

    public double imaginaryPart() {
        return im;
    }

    // 사칙연산 메소드들이 자기 자신을 변경하지 않고 새로운 Complex 인스턴스를 반환하고 있다. 각각의 인스턴스는 여전히 변하지 않았다.
    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }

    public Complex minus(Complex c) {
        return new Complex(re - c.re, im - c.im);
    }

    public Complex times(Complex c) {
        return new Complex(re * c.re - im * c.im, re * c.im + im * c.re);
    }

    public Complex dividedBy(Complex c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new Complex((re * c.re + im * c.im) / tmp, (im * c.re - re * c.im) / tmp);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Complex)) {
            return false;
        }

        Complex c = (Complex) o;

        return Double.compare(c.re, re) == 0 && Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    @Override
    public String toString() {
        return "(" + re + " + " + im + "i";
    }
}
```

불변 객체의 장점
- 불변 객체는 단순하다.
    - 생성시점의 상태를 파괴시점까지 유지할 수 있으니 개발자는 특별한 노력없이 객체의 상태를 믿고 사용할 수 있다.
- 불변 객체는 기본적으로 스레드 안전하여 따로 동기화 할 필요가 없다.
- 불변객체는 상태가 변하지 않으니 안심하고 공유할 수 있다. 위의 예시에선 자주 사용하는 값들을 상수화 해서 재활용 할 수 있다.

불변 객체의 단점
- 객체의 값이 다르면 무조건 독립된 다른 객체를 만들어야 된다.

위 코드에선 class를 final로 선언해 상속을 막는 코드이고, 두번째 방법인 모든 생성자를 private(혹은 default)로 만들고 static factory method를 만들어 제공하는 방법을 적겠다.

```java
public class Complex {
    private final double re;
    private final double im;

    private Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }

    ... 이하 생략
}
```

정리
- 클래스는 필요한 경우가 아니라면 무조건 불변이어야 한다. getter를 만들었다해서 습관적으로 setter를 만들지 말자.
- 불변으로 만들 수 없는 클래스는 변경을 최소화 하자.
- 다른 합당한 이유가 없다면 모든 필드는 private final로 선언하자.
- 생성자는 불변식 설정이 모두 완료된, 초기화가 완벽히 끝난 상태의 객체를 생성해야 한다. 확실한 이유가 없다면 생성자와 static factory method를 제외한 초기화 메소드는 public으로 제공하면 안된다.
---
## item18 상속보다는 컴포지션을 사용하라
일단 필자는 item18을 두번째 읽고있다. 첫번째 읽었을때 이해한 사실은 상속은 또 다른 의존성을 만든다. 이거 하나였다. 이 사실밖에 이해 못했다는 사실에 답답해서 짧은 시간에 두번 읽게 됐다.

상위클래스와 하위클래스 모두 같은 프로그래머가 통제하는 패키지안에서 관리되면 상속도 안전한 방법이다. 하지만 구체클래스를 패키지 경계를 넘어 상속하는건 위험하다. (여기서 구현은 인터페이스를 구현하거나 인터페이스가 다른 인터페이스를 확장하는 것이 아닌 클래스가 다른 클래스를 확장하는걸 말한다.)

상속은 캡슐화를 깨뜨린다.

```java
// 잘못된 예
// 생성이후 요소가 몇개 추가됐는지 알려주는 기능을 제공함.
public class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;
    
    public InstrumentedHashSet() {

    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCcap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```
```java
InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
s.addAll(List.of("s", "ss", "sss"));
System.out.println(s.getAddCount());    // 3을 기대하고 있다.
```

우리는 getAddCount 메소드가 3을 반환해줄거라 기대하겠지만, 실제로는 6을 반환한다. 왜 그럴까? 원인은 addAll함수가 add함수를 이용해 구현되있기 때문이다.
HashSet의 addAll은 각 원소들을 대상으로 add메소드를 호출해 추가하는데(이런 사실은 공식문서에는 작성이 안되있다.) 여기서 add메소드는 우리가 InstrumentedHashSet에서 재정의한 add메소드이다. 

중요한점은 이러한 문제가 개발자가 컨트롤할 수 없으면서 확장을 고려하지 않고 설계된 클래스를 상속받으면서 생긴 문제라는 것이다.

컴포지션은 이러한 문제를 해결하기 위한 방법이다.

```java
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> s;

    // 외부에서 주입받은 set을 이용함.
    // 어떤 set이 들어올진 모르겠지만 구체적인 Set이 들어옴
    // Set의 구현체이지만 구체적인 로직을 구현하지 않고 주입받은 구체적인 Set구현체의 로직을 따름
    public ForwardingSet(Set<E> s) {
        this.s = s;
    }

    public void clear() {
        s.clear();
    }

    public boolean contains(Object o) {
        return s.contains(o);
    }

    public boolean isEmpty() {
        return s.isEmpty();
    }

    public int size() {
        return s.size();
    }

    public Iterator<E> iterator() {
        return s.iterator();
    }

    public boolean add(E e) {
        return s.add(e);
    }

    public boolean remove(Object o) {
        return s.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return s.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return s.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return s.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return s.retainAll(c);
    }

    public Object[] toArray() {
        return s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    @Override
    public boolean equals(Object o) {
        return s.equals(o);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
```

```java
public class InstrumentedSet<E> extends ForwardingSet<E> {

    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```
책을 읽다가 ForwardingSet의 역할에대한 의문이 생겼다.
ForwardingSet을 거치지않고 Set 인터페이스를 바로 구현하는형태로 InstrumentedSet을 구현해도 똑같이 동작한다. 그렇다면 ForwardingSet의 존재이유는 뭘까...?

다음은 Set 인터페이스를 바로 구현한 InstrumentedSet 클래스이다.

```java
public class InstrumentedSet<E> implements Set<E> {
    private final Set<E> s;
    private int addCount = 0;

    public ExampleSet(Set<E> s) {
        this.s = s;
    }

    public void clear() {
        s.clear();
    }

    public boolean contains(Object o) {
        return s.contains(o);
    }

    public boolean isEmpty() {
        return s.isEmpty();
    }

    public int size() {
        return s.size();
    }

    public Iterator<E> iterator() {
        return s.iterator();
    }

    public boolean add(E e) {
        addCount++;
        return s.add(e);
    }

    public boolean remove(Object o) {
        return s.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return s.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return s.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return s.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return s.retainAll(c);
    }

    public Object[] toArray() {
        return s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    @Override
    public boolean equals(Object o) {
        return s.equals(o);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }

    public int getAddCount() {
        return addCount;
    }
}
```
외부에서 구체적인 Set구현체를 주입받아 구체적인 구현과는 멀어지고 원하는 정책을 추가하는 모습까지 똑같다. 단지 ForwardingSet이라는 전달 클래스가 없을뿐이다.

여기서 다른 클래스를 하나 추가할건데 이번엔 addCount가 아닌 deleteCount, 즉 생성이후 삭제된 원소의 갯수를 기록하는 정책을 갖는 클래스를 만들어 볼거다.

```java
public class MyDeleteSet<E> implements Set<E> {

    private final Set<E> s;
    private int deleteCount = 0;

    public MyDeleteSet(Set<E> s) {
        this.s = s;
    }

    public void clear() {
        s.clear();
    }

    public boolean contains(Object o) {
        return s.contains(o);
    }

    public boolean isEmpty() {
        return s.isEmpty();
    }

    public int size() {
        return s.size();
    }

    public Iterator<E> iterator() {
        return s.iterator();
    }

    public boolean add(E e) {
        deleteCount++;
        return s.add(e);
    }

    public boolean remove(Object o) {
        deleteCount++;
        return s.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return s.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        deleteCount += c.size();
        return s.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        deleteCount += c.size();
        return s.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return s.retainAll(c);
    }

    public Object[] toArray() {
        return s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    @Override
    public boolean equals(Object o) {
        return s.equals(o);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }

    public int getDeleteCount() {
        return deleteCount;
    }
}
```
Set 인터페이스를 직접 구현하면 Set인터페이스에 정의된 모든 메소드들을 구현해야된다. 또 다른정책을 갖는 다른 클래스를 만들게 된다면 이러한 반복적인 작업을 또 해야될것이다.

Set 인터페이스에서 정의된 메소드들을 공통으로 구현하되 구체적인 구현체는 외부에서 주입받는 형식으로 전달클래스를 만든뒤 전달클래스를 상속받고 필요한 정책만 재정의하면 코드의 중복을 없앨수 있을것이다.

```java
public class MySet<E> extends ForwardingSet<E> {

    private int deleteCount = 0;

    public MySet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean remove(Object o) {
        deleteCount++;
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        deleteCount += c.size();
        return super.removeAll(c);
    }

    public int getDeleteCount() {
        return deleteCount;
    }
}
```

위 예시처럼 컴포지션 방식으로 한번 구현해두면 HashSet뿐만아니라 어떤 Set구현체로도 활용할 수 있다.

Decorator Pattern 공부하기!

---
## item19 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

---
## item20 추상 클래스보다는 인터페이스를 우선하라
추상클래스가 정의한 타입을 구현하려면 반드시 추상클래스의 하위 클래스여야 된다. 자바는 다중상속이 허용되지 않은 언어이다. 인터페이스는 다중구현 및 다중상속이 가능하기 때문에 추상클래스보다 인터페이스가 더 우선시 되어야 한다.

예를들어 가수 인터페이스와 작곡가 인터페이스가 있다고 가정해보자

```java
public interface Singer{
    AudioClip sing(Song s);
}

public interface SongWriter {
    Song compose(int chartPosition);
}
```

현실세계에선 작곡도 하는 가수, 즉 싱어송라이터가 있다. 위에서 인터페이스로 정의했기 때문에 두 인터페이스 모두 상속받고 새로운 메소드를 정의할 수 있다.

```java
public interface SingerSongWriter extends Singer, SongWriter {
    AudioClip strum();
    void actSensitive();
}
```

만약 Singer와 SongWriter가 인터페이스가 아니라 클래스로 정의되었다면 꽤 복잡하게 접근해야 할 것이다.

책에서 나온 디자인패턴) template method pattern, adapter pattern

---
## item21 인터페이스는 구현하는 쪽을 생각해 설계하라
Java 8 이전에는 기존 구현체를 깨뜨리지 않고서는 인터페이서에 새로운 메소드를 추가 할 수 없었다. 
Java 8 부터는 default method를 이용해 기존 인터페이스에 새로운 메소드를 추가 할 수 있게됐지만 문제가 완벽히 해결된 것은 아니다. 왜냐면 default method를 추가한다는건 인터페이스를 구현한 모든 구현클래스를 모른채 합의없는 메소드 삽입이 될 수 있기 때문이다.

새로운 인터페이스를 정의할때 default method는 표준적인 메소드 구현을 제공해주는 역할을 할 수 있지만 기존 인터페이스에 default 메소드를 추가하는 일은 정말 꼭 필요한 경우가 아니라면 하지말자. 사용하는곳에서 무슨 재앙을 맞이할지 모른다.

---
## item22 인터페이스는 타입을 정의하는 용도로만 사용하라
인터페이스는 외부에 공개할 API를 정의해 사용하기도 하고, 구현체의 인스턴스를 참조할 수 있는 타입 역할을 한다. 즉, 공개한 API는 사용하고 내부 구조까지 파악할 수 없게끔 해준다. 인터페이스는 이러한 역할로만 사용해야 한다.

인터페이스에는 메소드뿐만아니라 상수도 작성할 수 있다는 것 때문에 메소드없이 상수만 작성하는 상수 인터페이스를 만들 수 있다.(상수 인터페이스는 안티패턴이다.)

```java
// 다시 적지만 상수 인터페이스는 안티패턴이다.
public interface PhysicalConstants {
    static final double AVOHADROS_NUMBER = 6.022_140_857e23;
    static final double BOLTMANN_CONSTANT = 1.380_648_52e-23;
    static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```
상수 인터페이스가 안티패턴인 이유
- 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아닌 내부 구현이다. 즉 인터페이스에서 내부 구현을 노출한 셈이 된다.
- 클라이언트코드에서 내부 구현에 해당되는 상수들에 종속되게 할 수 있다.

상수를 제공할 목적이면 상수 인터페이스보단 유틸리티 클래스를 만들어 제공하는편이 훨 낫다.
```java
public class PhysicalConstants {
    // 인스턴스화를 막기위해 private 생성자를 선언
    private PhysicalConstants() {}

    public static final double AVOHADROS_NUMBER = 6.022_140_857e23;
    public static final double BOLTMANN_CONSTANT = 1.380_648_52e-23;
    public static final double ELECTRON_MASS = 9.109_383_56e-31;
    // java 7 버전부터 숫자사이에 밑줄을 허용해줘서 가독성을 높여준다.
}
```
---
## item23 태그 달린 클래스보다는 클래스 계층구조를 활용하라
```java
public class Figure {
    enum Shape { RECTANGLE, CIRCLE };

    final Shape shape;

    // 사각형일때만 쓰는 필드
    double length;
    double width;

    // 원일때만 쓰는 필드
    double radius;

    // 원용 생성자
    public Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // 사각형용 생성자
    public Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    double area() {
        switch(shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionsError(shape);
        }
    }
}
```

현재 도형이 무슨 도형인지 나타내는 태그 필드(shape)가 달린 클래스이다. 개인적인 생각이지만 책의 다른 내용을 안보고 이 클래스만 봤을때 들은 생각은 못생겼다. 아마 주석이 없었다면 간단한 클래스치고 읽는데 꽤 오래걸렸을 것이다. 여러개의 상태와 그 상태별로 달라지는 행동들이 한 곳에 혼합 되있어 가독성이 굉장히 떨어진다. 

추가로 사각형과 원이 아닌 다른 모양이 추가 된다면? 그 모양을 나타내는 enum을 추가해주고, 그 모양이 사용하는 필드를 추가해주고, area method안에 switch문을 수정해주고... 사람인지라 실수 할 수도 있고 실수를 하지 않더라도 이 클래스의 가독성은 더 떨어질것이다.

이런 태그 달린 클래스를 계층구조를 갖는 클래스들로 리팩토링 하자.

- 가장 먼저 root를 나타낼 추상클래스를 정의하고, 태그값별로 행동이 달라지는 메소드를 추상클래스로 정의한다.
- 태그값과 상관없이 행동이 일정한 메소드들은 일반메소드로 추가한다.
- 공통으로 사용하는 데이터 필드들은 root class에 정의한다.

리팩토링한 결과는 이렇게 될것이다.

```java
public abstract class Figure {
    public abstract double area();
}

public class Circle extends Figure {
    final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * (radius * radius);
    }
}

public class Rectangle extends Figure {
    final double length;
    final double width;

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double area() {
        return length * width;
    }
}
```

계층구조로 구현하니 클래스 이름 자체가 의미를 갖고 있으니 태그가 필요없어지고 클래스안에는 각 의미에 맞는 필드들만 존재하게 된다. 메소드도 더이상 태그별로 구분하기 위한 switch문이 필요없어졌다. 그리고 새로운 도형을 추가하기 위해서 기존 코드에 가독성을 해치는 행위를 하지 않아도 된다.

```java
public class Square extends Rectangle {
    public Square(double side) {
        super(side, side);
    }
}
```
---
## item24 멤버 클래스는 되도록 static으로 만들라
중첩 클래스는 자신을 감싼 바깥 클래스에서만 쓰여야하며, 그외의 쓰임새가 있다면 톱레벨 클래스로 만들자.

중첩 클래스의 종류는 static 멤버 클래스, 멤버 클래스, 익명 클래스, 지역 클래스 이렇게 있고 이 중 static 멤버 클래스를 제외한 나머지는 inner class이다.

    
static 멤버 클래스는 다른 클래스 안에 선언되고 바깥 클래스의 private 멤버에 접근할 수 있다는 점 빼면 일반클래스와 똑같다.
static 멤버 클래스는 바깥 클래스의 인스턴스와 암묵적으로 연결되있지 않다. 그래서 클래스 내부에서 바깥 클래스의 멤버 필드에 접근할 수 없다.
```java
public class Example {
    private int num = 0;
    static class StaticExample {
        public int getNum() {
            return num; // compile error!!!
        }
    }
}
```
하지만 비정적 멤버 클래스의 인스턴스는 바깥 클래스 인스턴스와 암묵적으로 연결된다. 그래서 비정적 멤버 클래스의 인스턴스 메소드에서 정규화된 this(클래스명.this)를 이용해 바깥 클래스의 메소드나 바깥 인스턴스의 참조를 가져올 수 있다.
```java
public class Example {
    private int num = 0;
    InnerExample innerExample;
    public Example() {
        this.innerExample = new InnerExample();
    }
    class InnerExample {
        private int num = 1;
        public void printNum() {
            System.out.println("this.num = " + this.num);
            System.out.println("Example.this = " + Example.this.num);
        }
    }
}
```
비정적 클래스는 어댑터를 정의할때 많이 사용된다.
멤버 클래스에서 바깥클래스의 인스턴스에 접근할 일이 없으면 무조건 static 멤버클래스로 선언하자.
- 바깥 인스턴스로의 숨은 외부 참조를 갖게 되므로, 이를 저장하기 위해 시간과 공간이 소모된다.
- 가비지 컬렉션이 바깥 클래스의 인스턴스를 수거하지 못해서 메모리 누수가 발생한다.
- 참조가 눈에 보이지 않으니 문제의 원인을 찾기 힘들다.
---
## item25 톱레벨 클래스는 한 파일에 하나만 담으라
이번 아이템은 간단하다 톱레벨 클래스는 한 파일에 하나만 담자. 하나의 파일에 여러 톱레벨 클래스를 작성해도 우리의 착한 자바 컴파일러는 아무말도 안하지만 동료 개발자가 이 사실을 모르고 같은 이름을 갖는 톱레벨 클래스를 중복해서 선언할 수 있다. 

```java
// Utensil.java에 선언된 두개의 톱레벨 클래스
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```

```java
// Dessert.java에 선언된 두개의 톱레벨 클래스
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```