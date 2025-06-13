package com.research.oproom

class Post(private var id: Int, private var temperature: Float, private var humidity: Float) {
    public fun getId(): Int{
        return this.id
    }
    public fun getTemperature(): Float{
        return this.temperature
    }
    public fun getHumidity(): Float{
        return this.humidity
    }

    public fun setId(id: Int){
        this.id = id
    }
    public fun setTemperature(temperature: Float){
        this.temperature = temperature
    }
    public fun setHumidity(humidity: Float){
        this.humidity = humidity
    }

}