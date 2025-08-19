package com.example.lugarescomunes.api;

import com.example.lugarescomunes.models.api.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface LugaresApiService {

    // ===== AUTENTICACIÓN =====

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest registerRequest);

    @GET("auth/me")
    Call<ApiResponse<UserResponse>> getCurrentUser();

    @GET("auth/validate-email")
    Call<ApiResponse<EmailValidationResponse>> validateEmail(@Query("email") String email);

    @GET("auth/health")
    Call<ApiResponse<Object>> authHealth();

    // ===== LUGARES (ENDPOINTS PÚBLICOS) =====

    @GET("places")
    Call<ApiResponse<List<PlaceResponse>>> getAllPlaces();

    @GET("places/{id}")
    Call<ApiResponse<PlaceResponse>> getPlaceById(@Path("id") String id);

    @GET("places/search")
    Call<ApiResponse<List<PlaceResponse>>> searchPlaces(@Query("q") String query);

    @GET("places/type/{type}")
    Call<ApiResponse<List<PlaceResponse>>> getPlacesByType(@Path("type") String type);

    @GET("places/available")
    Call<ApiResponse<List<PlaceResponse>>> getAvailablePlaces();

    @GET("places/building/{name}")
    Call<ApiResponse<List<PlaceResponse>>> getPlacesByBuilding(@Path("name") String buildingName);

    @GET("places/what3words")
    Call<ApiResponse<PlaceResponse>> getPlaceByWhat3words(@Query("code") String code);

    @GET("places/nearby")
    Call<ApiResponse<List<PlaceResponse>>> getNearbyPlaces(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("radius") double radiusKm
    );

    // ===== LUGARES (ENDPOINTS ADMIN) =====

    @POST("places")
    Call<ApiResponse<PlaceResponse>> createPlace(@Body CreatePlaceRequest request);

    @PUT("places/{id}")
    Call<ApiResponse<PlaceResponse>> updatePlace(@Path("id") String id, @Body UpdatePlaceRequest request);

    @DELETE("places/{id}")
    Call<ApiResponse<Object>> deletePlace(@Path("id") String id);

    // ===== RUTAS (ENDPOINTS PÚBLICOS) =====

    // ✅ CORREGIDO: Usar PlaceResponse en lugar de RouteDestinationResponse
    // Según las imágenes que el usuario envió, este endpoint retorna lugares directamente
    @GET("routes/destinations")
    Call<ApiResponse<List<PlaceResponse>>> getRouteDestinations();

    @GET("routes/to/{placeId}")
    Call<ApiResponse<List<RouteResponse>>> getRoutesToPlace(@Path("placeId") String placeId);

    @GET("routes/{routeId}/points")
    Call<ApiResponse<List<RoutePointResponse>>> getRoutePoints(@Path("routeId") String routeId);

    @GET("routes/nearest")
    Call<ApiResponse<RouteResponse>> getNearestRoute(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("destination") String destinationId
    );

    @GET("routes/{routeId}/details")
    Call<ApiResponse<RouteDetailsResponse>> getRouteDetails(@Path("routeId") String routeId);

    @GET("routes/health")
    Call<ApiResponse<Object>> routesHealth();

    // ===== RUTAS (ENDPOINTS PROTEGIDOS) =====

    @POST("routes/{routeId}/rating")
    Call<ApiResponse<RatingResponse>> rateRoute(@Path("routeId") String routeId, @Body RateRouteRequest request);

    @GET("routes/{routeId}/my-rating")
    Call<ApiResponse<RatingResponse>> getMyRating(@Path("routeId") String routeId);

    // ===== PROPUESTAS DE RUTAS (ENDPOINTS PROTEGIDOS) =====

    @POST("routes/proposals")
    Call<ApiResponse<RouteProposalResponse>> submitRouteProposal(@Body RouteProposalRequest request);

    @GET("routes/proposals/my")
    Call<ApiResponse<List<RouteProposalResponse>>> getMyProposals();

    // ===== NAVEGACIÓN (ENDPOINTS PROTEGIDOS) =====

    @POST("navigation/start")
    Call<ApiResponse<NavigationResponse>> startNavigation(@Body StartNavigationRequest request);

    @POST("navigation/complete")
    Call<ApiResponse<NavigationResponse>> completeNavigation(@Body CompleteNavigationRequest request);

    @GET("navigation/history")
    Call<ApiResponse<List<NavigationHistoryResponse>>> getNavigationHistory();

    // ===== FAVORITOS (ENDPOINTS PROTEGIDOS) =====

    @GET("favorites")
    Call<ApiResponse<List<PlaceResponse>>> getFavorites();

    @POST("favorites/{placeId}")
    Call<ApiResponse<Object>> addFavorite(@Path("placeId") String placeId);

    @DELETE("favorites/{placeId}")
    Call<ApiResponse<Object>> removeFavorite(@Path("placeId") String placeId);

    // ===== HEALTH CHECKS =====

    @GET("auth/health")
    Call<ApiResponse<Object>> generalHealth();
}