package io.littlehorse.examples.controllers;

import io.littlehorse.examples.model.Coupon;
import io.littlehorse.examples.services.CouponService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/coupons")
@Produces(MediaType.APPLICATION_JSON)
public class CouponController {
    @Inject
    CouponService couponService;
    @GET
    @Path("/client/{clientId}")
    public Response getCouponsByClientId(@PathParam("clientId") long clientId) {
        List<Coupon> coupons = couponService.getAllCoupnsForClient(clientId);
        return Response.ok(coupons).build();
    }
}
