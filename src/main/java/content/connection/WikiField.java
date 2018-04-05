package content.connection;

public enum WikiField {
    Swieta(0, "Święta"),
    PolskaEvents(1, "Polska"),
    WorldEvents(2, "Świat"),
    Born(3, "Urodzili się"),
    Dead(4, "Zmarli");

    private int fieldNumber;
    private String fieldName;

    WikiField(int fieldNumber, String fieldName){
        this.fieldNumber = fieldNumber;
        this.fieldName = fieldName;
    }

    public int fieldNumber(){
        return fieldNumber;
    }

    public String fieldName(){
        return fieldName;
    }
}
