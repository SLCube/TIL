# 2장 객체 생성과 파괴

## item1 생성자 대신 static 팩토리 메소드를 고려하라.

클라이언트가 클래스의 인스턴스를 얻는 전통적인 방법은 public 생성자이다.
```java
Student s1 = new Student();
```

생성자말고 또 알아둬야될 방법이 정적 팩토리 메소드(static factory method)이다.

```java
public static Boolean valueOf(boolean b) {
    return b ? Boolean.TRUE : Boolean:FALSE;
}
```

생성자보다 static factory method가 좋은 이유 5가지
1. 이름을 갖을 수 있다. 생성자와 생성자에 넘기는 파라미터만으로 반환되는 객체의 특성을 정확히 설명할 수 없다.
2. 호출할 때마다 새로운 인스턴스를 새로 생성하지 않아도 된다.
3. 반환타입의 하위타입 객체를 반환할 수 있는 능력이 생긴다. 
4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.
5. static factory method를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

static factory method가 갖는 단점 2가지
1. 상속을 하려면 public or protected생성자가 필요하니 static factory method만 제공하면 하위클래스를 만들 수 없다.
2. static factory method는 개발자가 찾기 어렵다. 생성자처럼 api설명에 명확히 드러나지 않으니 api문서 정리를 잘해두지 않으면 찾기 어렵다. 

## item2 생성자에 매개변수가 많다면 빌더를 고려하라.
생성자와 static factory method에는 공통적인 단점이 하나 있는데 선택적 매개변수가 많을때 적절히 대응하기 어렵다는 것이다. 

식품 영양정보를 나타내는 클래스를 생각해보면 탄수화물, 나트륨, 지방, 단백질 등등... 영양소의 종류는 정말 많지만 많은 항목들의 값이 0으로 채워질 것이다.

이러한 상황을 해결해 나갈 3가지 방법

1. 점층적 생성자 패턴(telescoping constructor pattern)
```java
public class NutritionFacts {
    private final int servingSize;  // 1회 제공량
    private final int servings; // 총 n회 제공량
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingsize, servings, calories, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat) {
        this(servingSize, servings, calories, fat, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
        this(servingSize, servings, calories, fat, sodium, 0);
    }

    public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
```

위 클래스는 생성자의 매개변수가 6개밖에(?) 안돼서 그나마 실수할 확률이 줄어들겠지만 20개라면 40개라면? 사용하는데 실수할 확률도 올라갈거고 코드도 길어지고 읽기 어려울 것이다.

2. 자바빈즈 패턴
```java
public class NutritionFacts {
    private int servingSize = -1;
    private int servings = -1;
    private int calories = 0;
    private int fat = 0;
    private int sodium = 0;
    private int carbohydrate = 0;

    public NutritionFacts() {}

    // setter
    public void setServingSize(int val) {
        servingSize = val;
    }

    public void setServings(int val) {
        servings = val;
    }

    public void setCalories(int val) {
        calories = val;
    }

    public void setFat(int val) {
        fat = val;
    }

    public void setSodium(int val) {
        sodium = val;
    }

    public void setCarbohydrate(int val) {
        carbohydrate = val;
    }
}
```
자바빈즈패턴이 갖고있는 단점은 객체하나를 만드려면 여러개의 메소드를 호출 해야되고 객체를 불변으로 만들 수 없다. 

3. 빌터패턴
```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // 필수 매개변수;
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본 값으로 초기화
        private int calories = 0;
        private int fat = 0;
        private int sodium = 0;
        private int carbohydrate = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            calories = val;
            return this;
        }

        public Builder fat(int val) {
            fat = val;
            return this;
        }

        public Builder sodium(int val) {
            sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```
빌더 패턴을 사용하는 코드이다.
```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8).calories(100).sodium(35).cabohydrate(27).build();
```

빌더패턴은 계층적으로 설계된 클래스와 함께 쓰기 좋다.

```java
public abstract class Pizza {

    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> toppings;
    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}
```

```java
public class Calzone extends Pizza {

    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false;

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        public Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```

```java
public class NyPizza extends Pizza {

    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NyPizza build() {
            return new NyPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```

빌더패턴의 단점은 객체를 생성할 때 빌더부터 생성한다. 성능에 민감한 상황에서는 문제가 될 수 있다. 

추가로 빌더패턴을 직접 작성하기엔 코드가 길어지는 문제가 있다. Java진영에서 많이 쓰이는 Lombok 라이브러리의 @Buildeer 어노테이션을 이용하면 문제를 해결할 수 있을거같다.

## item3 private 생성자나 열거타입으로 싱글턴임을 보장하라.

싱글턴을 만드는 두가지 방식

일단 두 방식 모두 생성자는 private으로 막아둔다.

첫번째는 싱글톤 인스턴스에 접근할 수 있는 public static final멤버를 만든다.
```java
public class Example {
    public static final Example INSTANCE = new Example();

    private Example() {}

    ... 이하 생략
}
```

두번째는 static factory method에서 미리 선언한 인스턴스를 반환한다.
```java
public class Example {
    private static final Example INSTANCE = new Example();

    private Example() {}

    public Example getInstance() {
        return INSTANCE;
    }
}
```

두번째 방식은 첫번째 방식과 비교했을때 어떤 장점이 있을까?

1. 마음이 바뀌면 싱글턴이 아니게 바꿀수있다. 
1. static factory method를 제네릭 싱글톤 팩토리 메소드로 바꿀수있다.(item30 학습 후 정리)
1. static factory method의 메소드 참조를 공금자로 사용할 수 있다.(item43, 44)

이런 장점이 필요 없다면 첫번째 방식을 선택하자.

세번째는 원소가 하나인 열거타입을 선언하는 것이다.
```java
public enum Example {
    INSTANCE;
    
    ...
}
```
public 방식과 비슷하지만 더 간결하고, 추가 노력없이 직렬화할 수 있고, 리플렉션 공격에도 안전한다.

Enum외의 클래스를 상속해야한다면 세번째 방법은 사용할 수 없다.

## item4 인스턴스화를 막으려면 private 생성자를 사용하라

static 멤버만 담은 유틸 클래스는 인스턴스를 만들어 사용하라고 설계한것이 아니다. 하지만 생성자를 명시하지 않으면 자동으로 기본 public생성자를 만들어지고 사용하는 입장에서 개발자가 선언한건지 자동으로 생성된건지 판단할 수 없다.

추상클래스로 만드는것으로는 인스턴스화를 막을 수 없다. 하위클래스를 만들면 되는것이다. 오히려 추상클래스로 선언하는 행위자체가 상속해서 사용하라는 의미로 오해할 수 있다.

private 생성자를 선언하면 인스턴스화 자체를 막을 수 있다.

## item5 자원을 직접 명시하지 말고 의존 객체 주입을 사용하자

## item6 불필요한 객체 생성을 피하라

## item7 다 쓴 객체 참조를 해제하라

## item8 finalizer와 cleaneer 사용을 피하라

## item9 try-finally 보다는 try-with-resources를 사용하라