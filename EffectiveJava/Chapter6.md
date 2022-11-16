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

