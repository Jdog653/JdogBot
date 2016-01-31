/**
 * Created by Jordan on 8/6/2015.
 */
public enum LMMoney
{
    GOLD_COIN(5000),
    DOLLAR_BILL(20000),
    GOLD_BAR(100000),
    SAPPHIRE(500000),
    EMERALD(800000),
    RUBY(1000000),
    SILVER_DIAMOND(2000000),
    RED_DIAMOND(5000),
    GOLD_DIAMOND(20000000),
    SMALL_PEARL(50000),
    MEDIUM_PEARL(100000),
    LARGE_PEARL(1000000);

    private int value;

    LMMoney(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static LMMoney getEnum(String value)
    {
        for(LMMoney m : values())
        {
            if(m.toString().equalsIgnoreCase(value))
            {
                return m;
            }
        }

        throw new IllegalArgumentException();
    }

    public String toString()
    {
        return super.toString().replace("_", "").toLowerCase();
    }
}
