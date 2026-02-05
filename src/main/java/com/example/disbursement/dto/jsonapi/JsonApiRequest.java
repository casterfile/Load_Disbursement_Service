package com.example.disbursement.dto.jsonapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonApiRequest<T> {
    private JsonApiData<T> data;
}
