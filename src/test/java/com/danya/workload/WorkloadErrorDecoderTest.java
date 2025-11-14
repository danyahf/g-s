package com.danya.workload;

import com.danya.workload.api.client.WorkloadErrorDecoder;
import com.danya.workload.api.exception.ClientErrorException;
import com.danya.workload.api.exception.InvalidRequestException;
import com.danya.workload.api.exception.ResourceNotFoundException;
import com.danya.workload.api.exception.WorkloadServiceException;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkloadErrorDecoderTest {
    private WorkloadErrorDecoder errorDecoder;

    @BeforeEach
    void createErrorDecoder() {
        this.errorDecoder = new WorkloadErrorDecoder();
    }

    @Test
    void shouldMap400ErrorToInvalidRequestException() throws IOException {
        //arrange
        String errorJson = """
        {
            "status": 400,
            "error": "Client Error",
            "message": "Message",
            "path": "/workloads"
        }
        """;

        Response mockResponse = createMockResponse(400, errorJson);

        //act
        Exception exception = errorDecoder.decode("createWorkload", mockResponse);

        //assert
        assertThat(exception).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldMap404ErrorToResourceNotFoundException() throws IOException {
        //arrange
        String errorJson = """
        {
            "status": 404,
            "error": "Client Error",
            "message": "Message",
            "path": "/workloads"
        }
        """;

        Response mockResponse = createMockResponse(404, errorJson);

        //act
        Exception exception = errorDecoder.decode("createWorkload", mockResponse);

        //assert
        assertThat(exception).isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 409, 412})
    void shouldMap4xxToClientErrorResponse(int statusCode) throws IOException {
        //arrange
        String errorJson = """
        {
            "status": %s,
            "error": "Client Error",
            "message": "Message",
            "path": "/workloads"
        }
        """.formatted(statusCode);

        Response mockResponse = createMockResponse(statusCode, errorJson);

        //act
        Exception exception = errorDecoder.decode("createWorkload", mockResponse);

        //assert
        assertThat(exception).isInstanceOf(ClientErrorException.class);
    }

    @Test
    void shouldMapServerErrorToWorkloadServiceException() throws IOException {
        //arrange
        String errorJson = """
        {
            "status": 500,
            "error": "Server Error",
            "message": "Message",
            "path": "/workloads"
        }
        """;

        Response mockResponse = createMockResponse(500, errorJson);

        //act
        Exception exception = errorDecoder.decode("createWorkload", mockResponse);

        //assert
        assertThat(exception).isInstanceOf(WorkloadServiceException.class);
    }

    @Test
    void shouldHandleUnparseableResponse() throws IOException {
        //arrange
        Response.Body mockBody = mock(Response.Body.class);
        Response mockResponse = mock(Response.class);

        when(mockResponse.status()).thenReturn(500);
        when(mockResponse.reason()).thenReturn("Internal Server Error");
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenThrow(new IOException("Stream closed"));

        //act
        Exception exception = errorDecoder.decode("createWorkload", mockResponse);

        //assert
        assertThat(exception).isInstanceOf(WorkloadServiceException.class);
        assertThat(exception.getMessage()).isEqualTo("Unable to parse error details from workload service");
    }

    private Response createMockResponse(int status, String bodyJson) throws IOException {
        Response.Body mockBody = mock(Response.Body.class);
        when(mockBody.asInputStream()).thenAnswer(invocation ->
                new ByteArrayInputStream(bodyJson.getBytes(StandardCharsets.UTF_8))
        );

        Response mockResponse = mock(Response.class);
        when(mockResponse.status()).thenReturn(status);
        when(mockResponse.body()).thenReturn(mockBody);

        return mockResponse;
    }
}
