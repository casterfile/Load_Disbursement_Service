package com.example.disbursement.dto.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonApiResponse<T> {
    private Object data;  // Can be JsonApiData<T> or List<JsonApiData<T>>
    private List<JsonApiError> errors;

    public static <T> JsonApiResponse<T> single(JsonApiData<T> data) {
        return JsonApiResponse.<T>builder().data(data).build();
    }

    public static <T> JsonApiResponse<T> list(List<JsonApiData<T>> dataList) {
        return JsonApiResponse.<T>builder().data(dataList).build();
    }

    public static <T> JsonApiResponse<T> error(List<JsonApiError> errors) {
        return JsonApiResponse.<T>builder().errors(errors).build();
    }
}
