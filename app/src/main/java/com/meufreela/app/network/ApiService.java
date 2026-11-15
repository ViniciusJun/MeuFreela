package com.meufreela.app.network;

import com.meufreela.app.models.request.CadastroRequest;
import com.meufreela.app.models.request.CriarAvaliacaoRequest;
import com.meufreela.app.models.request.CriarServicoRequest;
import com.meufreela.app.models.request.CriarSolicitacaoRequest;
import com.meufreela.app.models.request.EnviarMensagemRequest;
import com.meufreela.app.models.request.FcmTokenRequest;
import com.meufreela.app.models.request.LoginRequest;
import com.meufreela.app.models.request.TornarFreelancerRequest;
import com.meufreela.app.models.response.Avaliacao;
import com.meufreela.app.models.response.Freelancer;
import com.meufreela.app.models.response.LoginResponse;
import com.meufreela.app.models.response.Mensagem;
import com.meufreela.app.models.response.PerfilUsuario;
import com.meufreela.app.models.response.Servico;
import com.meufreela.app.models.response.Solicitacao;
import com.meufreela.app.models.response.SolicitacaoKycResponse;



import okhttp3.MultipartBody;
import okhttp3.ResponseBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- Autenticação ---
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/cadastro")
    Call<LoginResponse> cadastrar(@Body CadastroRequest request);

    // --- Freelancers ---
    @GET("freelancers")
    Call<List<Freelancer>> listarFreelancers(@Query("categoria") String categoria);

    @GET("freelancers/{id}")
    Call<Freelancer> detalheFreelancer(@Path("id") String id);

    // --- Solicitações ---
    @POST("solicitacoes")
    Call<Solicitacao> criarSolicitacao(@Body CriarSolicitacaoRequest request);

    @GET("solicitacoes/como-cliente")
    Call<List<Solicitacao>> solicitacoesComoCliente();

    @GET("solicitacoes/como-freelancer")
    Call<List<Solicitacao>> solicitacoesComoFreelancer();

    @PATCH("solicitacoes/{id}/status")
    Call<Solicitacao> atualizarStatus(
            @Path("id") String id,
            @Query("status") String status);

    // --- Chat ---
    @GET("solicitacoes/{id}/mensagens")
    Call<List<Mensagem>> historicoChat(@Path("id") String solicitacaoId);

    @POST("solicitacoes/{id}/mensagens")
    Call<Mensagem> enviarMensagemRest(
            @Path("id") String solicitacaoId,
            @Body EnviarMensagemRequest req);

    // --- Avaliações ---
    @POST("avaliacoes")
    Call<Avaliacao> criarAvaliacao(@Body CriarAvaliacaoRequest request);

    @GET("avaliacoes/usuario/{id}")
    Call<List<Avaliacao>> avaliacoesDoUsuario(@Path("id") String id);

    // --- Usuário ---
    @GET("usuarios/me")
    Call<PerfilUsuario> meuPerfil();

    @POST("usuarios/fcm-token")
    Call<ResponseBody> salvarFcmToken(@Body FcmTokenRequest request);

    @POST("usuarios/tornar-freelancer")
    Call<ResponseBody> tornarFreelancer(@Body TornarFreelancerRequest request);

    @POST("usuarios/tornar-cliente")
    Call<ResponseBody> tornarCliente();

    // Serviços
    @GET("servicos")
    Call<List<Servico>> listarServicos(@Query("categoria") String categoria);

    @GET("servicos/meus")
    Call<List<Servico>> meusServicos();

    @GET("servicos/{id}")
    Call<Servico> detalheServico(@Path("id") String id);

    @POST("servicos")
    Call<Servico> criarServico(@Body CriarServicoRequest req);

    @PUT("servicos/{id}")
    Call<Servico> atualizarServico(@Path("id") String id, @Body CriarServicoRequest req);

    @PATCH("servicos/{id}/ativo")
    Call<Servico> alternarServicoAtivo(@Path("id") String id);

    @DELETE("servicos/{id}")
    Call<Void> excluirServico(@Path("id") String id);
    @Multipart
    @POST("usuarios/foto")
    Call<ResponseBody> uploadFoto(@Part MultipartBody.Part file);

    @Multipart
    @POST("servicos/{id}/fotos")
    Call<Servico> uploadFotoServico(
            @Path("id") String servicoId,
            @Part MultipartBody.Part file);

    @DELETE("servicos/{id}/fotos")
    Call<Servico> removerFotoServico(
            @Path("id") String servicoId,
            @Query("url") String url);
<<<<<<< HEAD
}
=======

    @Multipart
    @POST("usuarios/kyc")
    Call<Void> enviarDocumentosKyc(
            @Part MultipartBody.Part documento,
            @Part MultipartBody.Part selfie
    );

    @GET("usuarios/kyc")
    Call<SolicitacaoKycResponse> statusKyc();
}
>>>>>>> f5e2371 (feat: implementa fluxo completo de KYC (envio de docs, painel de admin e validacao no app))
