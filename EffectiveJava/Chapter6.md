# 열거 타입과 어노테이션

## item34 int 상수 대신 열거 타입을 사용하라

다음은 정수 열거 패턴을 사용한 코드이다.
```java
public static final int APPLE_FUJI = 0;
public static final int APPLE_PIPPIN = 1;
public static final int APPLE_GRANNY_SMITH = 2;
public static final int ORANGE_NAVEL = 0;
public static final int ORANGE_TEMPLE = 1;
public static final int ORANGE_BLOOD = 2;
```

정수 열거 패턴은 문제가 많다. 가장 중요한 문제는 인간이 정한 특정 상수값을 0, 1, 2로 표현했지만 컴퓨터는 결국 정수 0, 1, 2로 인식할 뿐이다. 즉 APPLE_FUJI와 ORANGE_NAVEL이 같다는 소리이다.

이런저런 대안이 있지만 결국 자바에서 지원하는 enum을 사용하는것이 가장 깔끔하다.

자바의 열거타입은 완전한 형태의 클래스이기 때문에 단순한 정숫값인 다른언어의 열거 타입보다 훨씬 강력하다.

상수하나당 자신의 인스턴스를 하나씩 만들어 public static final로 공개한다. 열거타입은 밖에서 접근할 수 있는 생성자를 제공하지 않으므로 사실상 final이다.

열거타입에는 메소드나 필드를 추가할 수 있고, 임의의 인터페이스를 구현할 수 있다. 어떨때 메소드나 필드를 추가할까? 어떤 상수에 연관된 데이터들을 내제시키고 싶고 그 데이터들의 연산 결과를 알려주는 메소드를 만들고 싶을 수 있다. 책에선 태양계의 여덟 행성을 열거타입으로 선언해 소개하고 있다.
```java
public enum Planet {
    MERCURY(3.302e+23, 2.439e6),
    VENUS(4.869e+24, 6.05e6),
    EARTH(5.975e+24, 6.378e6),
    MARS(6.419e+23, 3.393e6),
    JUPITER(1.899e+27, 7.149e7),
    SATURN(5.685e+26, 6.027e7),
    URANUS(8.683e+25, 2.556e7),
    NEPTUNE(1.024e+26, 2.477e7);    // 열거타입의 오른쪽 숫자들은 생성자에 넘기는 매개변수를 뜻한다


    private final double mass;
    private final double radius;
    private final double surfaceGravity;

    private static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
        surfaceGravity = G * mass / (radius * radius);
    }

    public double mass() {
        return mass;
    }

    public double radius() {
        return radius;
    }

    public double surfaceGravity() {
        return surfaceGravity;
    }

    public double surfaceWeight(double mass) {
        return mass * surfaceGravity;
    }
}
```

과거의 태양계에선 명왕성까지 포함시켰다. 명왕성이 사라진 시점에 행성 열거타입의 명왕성이라는 상수도 삭제시켜야 할것이다. 그럼 위의 열거타입을 사용하는 클라이언트 코드엔 어떤 변화가 일어날까?

첫째로 명왕성을 사용하지 않는 코드들은 아무런 변화가 없다. 설령 명왕성이 아닌 두번째에 존재하는 금성이 제거된다하더라도 아무런 변화가 없었을것이다. 

둘째로 명왕성을 사용하는 코드들은 적절한 컴파일 에러 메세지를 나타낼것이다.

또 다른 예시로 공부해보자. 계산기의 연산종류를 열거타입으로 선언하고 실제 타입 상수가 연산까지 해주길 바라고 코드를 짜보자. 가장 먼저 생각할 수 있는 방법은 switch문을 이용하는 것이다.

```java
public enum Operation {
    PLUS, MINUS, TIMES, DIVIDE;

    public double apply(double x, double y) {
        switch (this) {
            case PLUS: return x + y;
            case MINUS: return x - y;
            case TIMES: return x * y;
            case DIVIDE: return x / y;
        }
        throw new AssertionError("알 수 없는 연산 " + this);
    }
}
```

동작은 하지만 안이쁘다. 만약 나머지를 구하는 연산을 추가하고싶다면? 상수를 추가한뒤에 switch case를 추가안한다면? 알수없는 연산 런타임 오류를 나타내며 프로그램이 종료될 것이다. 

다행히 열거타입에는 상수별 메소드 구현을 제공한다.

```java
public enum Operation {
    PLUS {
        @Override
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS {
        @Override
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES {
        @Override
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE {
        @Override
        public double apply(double x, double y) {
            return x / y;
        }
    };

    public abstract double apply(double x, double y);
}
```

apply추상 메소드를 선언한 뒤 상수 바로옆에 apply메소드를 재정의 해준다. 새로운 상수를 추가하더라도 apply메소드를 재정의 해줘야되고 재정의하지 않는다면 컴파일오류를 발생한다.

필요한 원소를 컴파일타임에 다 알 수 있는 상수 집합이라면 항상 열거타입을 사용하자

## item35 ordinal 메소드 대신 인스턴스 필드를 사용하라

ordinal 메소드란? 열거 타입 상수를 하나의 정숫값에 대응시킨뒤 그 정수값을 반환해주는 메소드이다. 

ordinal메소드에 대한 첫 인상은 item34(int 상수 대신 열거 타입을 사용하라)의 조언과 반대되는 행동을 하는 메소드라 생각했다. 기껏 열거타입을 만들었더니 다시 정수타입을 반환하네? 

잘못된 예시를 알아보자

```java
public enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET, SEXTET, SEPTET, OCTET, NONET, DECTET;

    public int numOfMusicians() {
        return ordinal() + 1;
    }
}
```

솔로부터 10중주까지 연주자 수를 반환해주는 numOfMusicians 메소드를 정의했다. 

이 방식엔 문제점이 있는데, 첫째로 중복된 상수를 반환할 수 없다. 예를들어 복4중주(double quartet)은 8명이 연주를 하는데 이미 8중주가 8이라는 정수를 할당받았기 때문에 같은 정수를 할당받을 수 없다. 

두번째는 중간을 비워둘 수 없다. 예를들어 12명이 연주하는 3중 4중주(triple Quartet)을 선언하고 싶은데 11명이 연주하는 형식의 상수가 없다. 이런경우엔 11명이 연주하는 상수 자리에 더미상수를 선언해야될것이다. 이름만 들어도 거부감들지 않는가? 더미상수... 

해결책은 열거 타입 상수에 연결된 값은 ordinal메소드로 얻지 말고 인스턴스 필드에 저장하자.

```java
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5), SEXTET(6), SEPTET(7), OCTET(8)
    , NONET(9), DECTET(10);

    private final int numOfMusicians;
    Ensemble(int size) {
        this.numOfMusicians = size;
    }

    public int numOfMusicians() {
        return numOfMusicians;
    }
}
```

이렇게 선언하면 위에서 언급한 문제점들을 깔끔하게 해결할 수 있다.

## item39 명명 패턴보다 어노테이션을 사용하라
전통적으로 도구나 프레임워크가 특별히 다뤄야 할 프로그램 요소에는 딱 구분되는 명명패턴을 적용해왔다. 예를들면 테스트 프레임워크인 JUnit은 버전3까지 테스트 메소드 이름을 test로 시작해야 테스트가 작동됐다. 꽤 괜찮은 방법이지만 몇가지 문제점이 있다. 

첫째론 오타에 취약하다. 예를들어 testSafeOverride로 지어야되는걸 tsetSafeOverride라고 짓는다면 JUnit은 해당 메소드를 그냥 지나쳐버릴거다 

두번째론 해당 명명 패턴을 올바른 요소에만 사용하란 보장이 없다. 예를들어 해당 명명패턴은 메소드에 사용해야되는데, 어떤 개발자가 test로 시작하는 클래스명을 정의하고 해당 클래스에 있는 메소드들이 모두 테스트되길 바란다면? 불행하게도 그 개발자가 원하는대로 이루어지지 않을 것이다.

마지막으론 프로그램 요소를 매개변수로 전달할 방법이 딱히 없다. 예를들어 특정 예외를 던져야 성공하는 테스트가 있다고 가정해보자. 단순히 test로 시작하는 메소드는 특정 예외가 어떤 예외인지 알 수 없다. 메소드이름에 해당 예외를 적는 방법을 채택할 수 있지만 이 방법을 채택하면 가독성도 떨어지고 깨지기 쉽다. 
(해당 내용은 어노테이션의 매개변수로 해결했다.)


어노테이션은 위의 문제점들을 아주 효과적으로 해결해주므로 어노테이션이 할 수 있는 일을 명명패턴으로 처리할 이유는 없다. 

도구 제작자가 아니라면, 어노테이션을 직접 제작할 일은 없다. 하지만 자바 프로그래머라면 자바에서 제공하는 어노테이션을 예외없이 사용해야한다.

## item40 @Override 어노테이션을 일관되게 사용하라

@Override 어노테이션은 메소드 선언에만 달 수 있다. 이 어노테이션을 다았다는 건 상위 타입의 메소드를 재정의했다는 뜻이다. 이 어노테이션을 제때 사용하지 않을경우 어떤일이 일어나는지 살펴보자

```java
public class Bigram {
    private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Bigram b) {
        return b.first == first && b.second == second;
    }

    public int hashCode() {
        return 31 * first + second;
    }

    public static void main(String[] args) {
        Set<Bigram> s = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                s.add(new Bigram(ch, ch));
            }
            System.out.println("s.size() = " + s.size());
        }
    }
}
```

해당 코드를 실행해보면 260이 나온다 26이 나올거같은데 왜그럴까?

코드를 잘 찾아보면 별 문제 없어보인다. equals를 재정의했고, equals를 재정의할땐 항상 hashCode를 재정의하라는 사실을 잘 지켰다. 하지만 잘 찾아보면 equals를 재정의한게 아니다. 재정의가 아니라 다중정의이다. 이게 뭔말인고 하니 Object의 equals를 재정의하려면 Object타입의 매개변수를 받아야된다. 하지만 우리는 Bigram타입의 매개변수를 받았다. 이제 코드를 고쳐보자

```java
@Override
public boolean equals(Object o) {
    if(!(o instanceof Bigram)) {
        return false;
    }

    Bigram b = (Bigram) o;
    return b.first == first && b.second == second;
}
```

재정의하는 메소드에 의식적으로 @Override 어노테이션을 달아주면 컴파일러가 우리의 실수를 바로 알려줄것이다. 명심하자

## item41 정의하려는 것이 타입이라면 마커 인터페이스를 사용하라

마커 인터페이스란? 아무런 메소드도 없고 단지 구현하는 클래스가 특정 속성을 갖고있다는걸 표시해주는 인터페이스다. 예를들면 Serializable 인터페이스가 있다.

마커 어노테이션이 등장하면서 마커 인터페이스는 옛날 방식이라 생각할 수 있다. 하지만 이 둘은 각자의 쓰임새가 있다. 

먼저 마커 인터페이스가 마커 어노테이션보다 나은점 두가지를 살펴보자

첫번째, 마커 인터페이스는 이를 구현하는 인스턴스들을 구분하는 타입으로 사용가능하지만 어노테이션은 그러지 못한다

두번째, 마커 인터페이스는 적용대상을 더 정밀히 지정할 수 있다. @Target을 Element.TYPE으로 선언한 어노테이션의 경우 모든 타입에 달 수 있다. 그에 반해 인터페이스는 인터페이스 또는 클래스에만 적용할 수 있다.


마커 어노테이션이 더 나은점은 거대한 어노테이션 시스템의 지원을 받는다는 점이다. 

추가로 어노테이션을 활발히 활용하는 프레임워크에선 대부분의 경우 어노테이션을 사용하는 편이 더 좋다.

