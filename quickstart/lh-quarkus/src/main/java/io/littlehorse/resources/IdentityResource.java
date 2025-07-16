package io.littlehorse.resources;

import io.littlehorse.model.IdentityVerificationStatus;
import io.littlehorse.model.SearchBookmark;
import io.littlehorse.model.SearchIdentityVerificationStatusResponse;
import io.littlehorse.model.ValidateIdentityRequest;
import io.littlehorse.model.ValidateIdentityResponse;
import io.littlehorse.model.VerifyIdentityRequest;
import io.littlehorse.model.VerifyIdentityResponse;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.services.IdentityService;
import io.littlehorse.services.WorkflowsService;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

@Path("/identity-verification")
@Produces(MediaType.APPLICATION_JSON)
public class IdentityResource {

    private final IdentityService identityService;
    private final WorkflowsService workflowsService;

    public IdentityResource(IdentityService identityService, WorkflowsService workflowsService) {
        this.identityService = identityService;
        this.workflowsService = workflowsService;
    }

    @GET
    @Path("/status/search")
    public Response search(
            @QueryParam("status") String status,
            @QueryParam("email") String email,
            @QueryParam("bookmark") String bookmark) {
        SearchBookmark oldBookmark = null;

        if (Objects.nonNull(bookmark)) {
            oldBookmark = SearchBookmark.fromString(bookmark);
        }

        WfRunIdList wfRunIdList = workflowsService.search(status, email, oldBookmark);
        SearchIdentityVerificationStatusResponse response = identityService.getStatuses(wfRunIdList);

        return Response.ok(response).build();
    }

    @GET
    @Path("/status/{wfRunId}")
    public Response getStatus(@PathParam("wfRunId") String wfRunId) {
        IdentityVerificationStatus response = identityService.getStatus(wfRunId);

        return Response.ok(response).build();
    }

    @POST
    @Path("/verify")
    public Response verify(@Valid ValidateIdentityRequest request) {
        CorrelatedEvent correlatedEvent = identityService.validate(request.getEmail(), request.getIsValid());

        ValidateIdentityResponse response = ValidateIdentityResponse.fromCorrelatedEvent(correlatedEvent);

        return Response.ok(response).build();
    }

    @POST
    @Path("/start")
    public Response start(@Valid VerifyIdentityRequest request) {
        WfRunId wfRunId =
                identityService.startVerification(request.getFullName(), request.getEmail(), request.getSsn());

        VerifyIdentityResponse response = VerifyIdentityResponse.fromWfRunIdProto(wfRunId);

        return Response.ok(response).build();
    }
}
