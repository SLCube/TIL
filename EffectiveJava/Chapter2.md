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

