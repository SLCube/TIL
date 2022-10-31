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

    