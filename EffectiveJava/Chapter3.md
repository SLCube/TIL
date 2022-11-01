# 3장 모든 객체의 공통 메소드

## item 10 : equals는 일반 규약을 지켜 재정의하라.

다음과 같은 경우엔 재정의 하지 않는 것이 최선이다.
- 각 인스턴스가 본질적으로 고유하다.
- 인스턴스의 논리적 동치성을 검사할 일이 없다.
    - 값 자체를 비교해 동등한지 비교할일이 없으면 논리적 동치성을 검사할 일이 없다는 뜻.
- 상위 클래스에서 재정의한 equals가 하위 클래스에서도 딱 들어맞는다.
- 클래스가 private이거나 package-private이고 equals 메소드를 호출할일이 없다.

생각) 재정의를 하지 않는경우를 봤을때 '논리적 동치성을 검사할 일이 있을 때'가 아니면 재정의를 하지 말아야겠다 라고 생각했다.

그럼 언제 재정의 하는가. 논리적 동치성을 확인해야되는데 상위 클래스의 equals가 논리적 동치성을 비교하도록 정의되지 않았을 경우.
예를들면 Integer, String같이 값 클래스가 이 경우에 포함될것이다.

equals 메소드를 재정의할때 동치관계를 구현하며, 다음을 만족해야됨(밑에 나오는 '=' 기호는 수학에서의 등호이다.)
- 반사성 : x = x
- 대칭성 : x = y -> y = x
- 추이성 : x = y and y = z <-> x = z
- 일관성 : x.equals(y)를 몇번을 해도 항상 같은 값이 나와야함.
- not null

1. 반사성
    
    반사성은 객체는 자기 자신과 같아야된다. 이 조건을 불만족하기가 더 어려워보인다.

2. 대칭성
    위 수식( x = y <-> y = x )처럼 서로에대한 동치 여부가 같아야된다.
    다음의 경우를 보자
    ```java
    public class CaseInsensitiveString {
        private final String s;

        public CaseInsensitiveString(String s) {
            this.s = Objects.requireNonNull(s);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CaseInsensitiveString) {
                return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
            }

            if (o instanceof String) {
                return s.equalsIgnoreCase((String) o);
            }
            return false;        
        }
    }
    ```
    ```java
    CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
    String s = "polish";
    ```
    cis.equals(s)는 true가 나오지만 s.equals(cis)는 false가 나온다. 왜냐하면 CaseInsensitiveString은 String의 존재를 알고 대응하지만 String은 CaseInsensitiveString을 모른다.

3. 추이성

    추이성이란 x = y and y = z -> x = z 가 성립해야된다. 새로운 필드 추가를 위해 만들어진 상속 클래스에서 위 규약을 어길 수 있다.

    ```java

    public class Point {

        private final int x;
        private final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) {
                return false;
            }
            Point p = (Point) o;
            return p.x == x && p.y == y;
        }
    }
    ```
    ```java
    public class Color extends Point {
        private final Color color;

        public Color(int x, int y, Color color){
            super(x, y);
            this.color = Objects.requireNonNull(color);
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof Point)) {
                return false;
            }

            // color을 무시하고 비교
            if(!(o instanceof ColorPoint)) {
                return o.equals(this);
            }

            return super.equals(o) && ((ColorPoint) o).color == color;
        }
    }
    ```

    ```java
        ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
        Point p2 = new Point(1, 2);
        ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);

        System.out.println("p1.equals(p2) = " + p1.equals(p2));
        System.out.println("p2.equals(p3) = " + p2.equals(p3));
        System.out.println("p3.equals(p1) = " + p3.equals(p1));
    ```
    각각 true, true false가 나온다. p1, p3를 각각 p2랑 비교했을때 color를 무시하고 비교했지만 p1과 p3를 비교할땐 color를 무시할 수 없으니 추이성에 위배되는 코드이다.

    확장된 클래스에서 추가된 필드를 포함하며 equals를 구현하는 방법은 없지만 괜찮은 우회법이 하나 있다.

    Point를 상속하는 대신 Point객체를 필드로 만든 뒤 Point 뷰 메소드를 만드는 것이다.
    
    ```java
    public class ColorPoint {
        private final Point point;
        private final Color color;

        public ColorPoint(int x, int y, Color color) {
            point = new Point(x, y);
            this.color = Objects.requireNonNull(color);
        }

        public Point asPoint() {
            return point;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) {
                return true;
            }

            if(!(o instanceof ColorPoint)) {
                return false;
            }

            ColorPoint cp = (ColorPoint) o;
            return cp.point.equals(point) && cp.color.equals(color);
        }
    }
    ```

    정리

    꼭 필요한 경우가 아니라면 equals를 재정의 하지 말자. 그럼에도 equals 메소드를 재정의 해야된다면 다음 단계를 따라 재정의하자.

    1. == 연산자를 이용해 입력이 자기 자신의 참조인지 확인한다.
    2. instanceof 연산자를 이용해 입력이 올바른 타입인지 확인한다.
    3. 입력값을 올바른 타입으로 형 변환한다.
    4. 입력객체와 자기 자신이 갖고있는 핵심 필드들이 '모두' 일치하는지 확인한다.

## item11 : equals를 재정의 하려거든 hashCode도 재정의하라.
다음과같이 equals를 재정의한 Class있다 치자.
```java
public class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "지역코드");
        this.prefix = rangeCheck(prefix, 999, "프리픽스");
        this.lineNum = rangeCheck(lineNum, 9999, "가입자번호");
    }

    private static short rangeCheck(int val, int max, String arg) {
        if (val < 0 || val > max) {
            throw new IllegalArgumentException(arg + ": " + val);
        }
        return (short) val;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof PhoneNumber)) {
            return false;
        }

        PhoneNumber pn = (PhoneNumber) o;
        return areaCode == pn.areaCode && prefix == pn.prefix && lineNum == pn.lineNum;
    }

    @Override
    public int hashCode() {
        int result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        return result;
    }
}
```

해당 클래스의 인스턴스를 HashMap의 원소로 사용해보자
```java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "제니");
```
그 다음에 m.get(new PhoneNumber(707, 867, 5309))를 실행하면 결과가 어떻게 나올까? 곰곰히 생각해보자.


<br>
내가 이 내용을 다른데에서 봤다면 "제니"가 나올거라 예상했을거다. 코드에대해 설명하자면 우리는 equals를 두 객체가 물리적으로 같진 않더라도 논리적으로 동치이면 true를 반환하게 재정의했다. 하지만 물리적으론 다른 객체이기 때문에 두 객체의 hash값은 다를거고 위 코드의 결과는 null이 나올거다.

equals 메소드가 두 객체를 물리적인 비교를 하든 논리적인 비교를 하든 두 객체가 같다면 hash값도 같아야된다.

다음은 hashCode구현의 가장 안좋은 예이다.
```java
@Override
public int hashCode(){
    return 42;      // 사실 딱봐도 이렇게하면 안되겠다라는 생각이 든다.
}
```

다음은 hashCode구현의 좋은 예이다.
```java
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
}
```

간단하게 말하자면 equals를 재정의할때 사용한 핵심 원소들의 hashCode값을 이용해 hashCode를 재정의하고있다.

생각) equals와 hashCode 재정의는 아직 초보인 내가 봤을 때 실수할 가능성이 많아보인다. 책에서는 Google의 AutoValue라이브러리가 이를 대신해준다고 소개해줬고 찾아보니 Lombok도 비슷한 기능이 있는것같다. 나중에 둘을 비교하면서 공부해봐야겠다.

