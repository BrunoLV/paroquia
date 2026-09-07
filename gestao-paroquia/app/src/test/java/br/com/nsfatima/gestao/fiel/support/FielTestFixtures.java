package br.com.nsfatima.gestao.fiel.support;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
public final class FielTestFixtures { private FielTestFixtures(){} public static UUID criarFiel(FielJpaRepository r){return criarFiel(r,"Fiel de Teste");} public static UUID criarFiel(FielJpaRepository r,String nome){var f=new FielEntity(); UUID id=UUID.randomUUID(); f.setId(id);f.setNome(nome);f.setCpf(String.format("%011d", Math.floorMod(id.getLeastSignificantBits(), 100_000_000_000L)));f.setDataNascimento(LocalDate.of(1990,1,1));f.setEmail("teste-"+id+"@example.com");f.setCelular("11999999999");f.setAtivo(true);f.setCriadoEmUtc(Instant.now());f.setAtualizadoEmUtc(Instant.now());f.setCriadoPor("teste");f.setAtualizadoPor("teste");return r.save(f).getId();} }
