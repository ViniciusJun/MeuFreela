package com.meufreela.app.network;

import com.meufreela.app.models.request.CadastroRequest;
import com.meufreela.app.models.request.CriarSolicitacaoRequest;
import com.meufreela.app.models.request.LoginRequest;
import com.meufreela.app.models.response.Freelancer;
import com.meufreela.app.models.response.LoginResponse;
import com.meufreela.app.models.response.PerfilUsuario;
import com.meufreela.app.models.response.Solicitacao;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ----- Autenticação -----
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/cadastro")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    // ----- Freelancers -----
    @GET("freelancers")
    Call<List<Freelancer>> listarFreelancers(@Query("categoria") String categoria);

    @GET("freelancers/{id}")
    Call<Freelancer> detalheFreelancer(@Path("id") String id);

    // ----- Solicitações -----
    @POST("solicitacoes")
    Call<Solicitacao> criarSolicitacao(@Body CriarSolicitacaoRequest request);

    @GET("solicitacoes/como-cliente")
    Call<List<Solicitacao>> solicitacoesComoCliente();

    @GET("solicitacoes/como-freelancer")
    Call<List<Solicitacao>> solicitacoesComoFreelancer();

    @PATCH("solicitacoes/{id}/status")
    Call<Solicitacao> atualizarStatus(
            @Path("id") String id,
            @Query("status") String novoStatus);
    @GET("usuarios/me")
    Call<PerfilUsuario> meuPerfil();
}