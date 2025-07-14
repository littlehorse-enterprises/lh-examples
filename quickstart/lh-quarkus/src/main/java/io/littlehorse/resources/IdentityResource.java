package io.littlehorse.resources;

import io.littlehorse.model.IdentityVerificationStatusResponse;
import io.littlehorse.model.VerifyIdentityRequest;
import io.littlehorse.model.VerifyIdentityResponse;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.services.IdentityService;
import io.littlehorse.services.VariablesService;
import io.littlehorse.workflows.QuickstartWorkflow;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/identity")
@Produces(MediaType.APPLICATION_JSON)
public class IdentityResource {

    private final IdentityService identityService;
    private final VariablesService variablesService;

    public IdentityResource(IdentityService identityService, VariablesService variablesService) {
        this.identityService = identityService;
        this.variablesService = variablesService;
    }

    @GET
    @Path("/{wfRunId}")
    public Response get(@PathParam("wfRunId") String wfRunId) {
        Variable fullName = variablesService.get(QuickstartWorkflow.FULL_NAME, wfRunId);
        Variable email = variablesService.get(QuickstartWorkflow.EMAIL, wfRunId);
        Variable status = variablesService.get(QuickstartWorkflow.APPROVAL_STATUS, wfRunId);

        IdentityVerificationStatusResponse response =
                IdentityVerificationStatusResponse.fromProto(fullName, email, status);

        return Response.ok(response).build();
    }

    @POST
    @Path("/verify")
    public Response verify(@Valid VerifyIdentityRequest request) {
        String wfRunId =
                identityService.startIdentityVerification(request.getFullName(), request.getEmail(), request.getSsn());

        return Response.ok(new VerifyIdentityResponse(wfRunId)).build();
    }
}
