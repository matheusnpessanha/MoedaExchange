package br.com.ConversorMoedas.modelos;

import com.google.gson.annotations.SerializedName;

public record DadosDaConversaoMoedas(@SerializedName("result")
                                     String result,
                                     @SerializedName("time_last_update_unix")
                                     long time_last_update_unix,
                                     @SerializedName("time_next_update_unix")
                                     long time_next_update_unix,
                                     @SerializedName("conversion_rate")
                                     double conversion_rate) {
}
