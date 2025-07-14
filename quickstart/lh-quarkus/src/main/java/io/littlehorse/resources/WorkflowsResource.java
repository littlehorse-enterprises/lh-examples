package io.littlehorse.resources;

import com.google.protobuf.ByteString;
import io.littlehorse.model.WfRunIdListModel;
import io.littlehorse.sdk.common.proto.WfRunIdList;
import io.littlehorse.services.WorkflowsService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Objects;

@Path("/workflows")
@Produces(MediaType.APPLICATION_JSON)
public class WorkflowsResource {
    private final WorkflowsService service;

    public WorkflowsResource(WorkflowsService workflowsService) {
        this.service = workflowsService;
    }

    @GET
    public Response get(
            @QueryParam("status") String status,
            @QueryParam("email") String email,
            @QueryParam("bookmark") String bookmark) {
        ByteString byteStringBookmark = null;

        if (Objects.nonNull(bookmark)) {
            byteStringBookmark = ByteString.copyFrom(Base64.getDecoder().decode(bookmark));
        }

        WfRunIdList wfRunIdList = service.search(status, email, byteStringBookmark);

        return Response.ok(WfRunIdListModel.fromProto(wfRunIdList)).build();
    }
}
