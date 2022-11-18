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

