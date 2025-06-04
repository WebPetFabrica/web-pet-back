package br.edu.utfpr.alunos.webpet.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserSerializationTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void shouldSerializeAndDeserializeUser() throws Exception {
        User user = User.builder()
                .name("João")
                .surname("Silva")
                .email("joao@teste.com")
                .password("senha123")
                .celular("41999999999")
                .build();
        
        String json = objectMapper.writeValueAsString(user);
        User deserializedUser = objectMapper.readValue(json, User.class);
        
        assertThat(deserializedUser.getName()).isEqualTo("João");
        assertThat(deserializedUser.getSurname()).isEqualTo("Silva");
        assertThat(deserializedUser.getEmail()).isEqualTo("joao@teste.com");
        assertThat(deserializedUser.getUserType()).isEqualTo(UserType.USER);
        assertThat(deserializedUser.getDisplayName()).isEqualTo("João Silva");
    }
    
    @Test
    void shouldSerializeAndDeserializeONG() throws Exception {
        ONG ong = ONG.builder()
                .cnpj("12345678000190")
                .nomeOng("ONG Teste")
                .email("ong@teste.com")
                .password("senha123")
                .celular("41888888888")
                .endereco("Rua Teste, 123")
                .descricao("ONG para testes")
                .build();
        
        String json = objectMapper.writeValueAsString(ong);
        ONG deserializedOng = objectMapper.readValue(json, ONG.class);
        
        assertThat(deserializedOng.getCnpj()).isEqualTo("12345678000190");
        assertThat(deserializedOng.getNomeOng()).isEqualTo("ONG Teste");
        assertThat(deserializedOng.getUserType()).isEqualTo(UserType.ONG);
        assertThat(deserializedOng.getDisplayName()).isEqualTo("ONG Teste");
        assertThat(deserializedOng.getIdentifier()).isEqualTo("12345678000190");
    }
    
    @Test
    void shouldSerializeAndDeserializeProtetor() throws Exception {
        Protetor protetor = Protetor.builder()
                .nomeCompleto("Maria Santos")
                .cpf("12345678901")
                .email("maria@teste.com")
                .password("senha123")
                .celular("41777777777")
                .endereco("Av. Teste, 456")
                .capacidadeAcolhimento(5)
                .build();
        
        String json = objectMapper.writeValueAsString(protetor);
        Protetor deserializedProtetor = objectMapper.readValue(json, Protetor.class);
        
        assertThat(deserializedProtetor.getCpf()).isEqualTo("12345678901");
        assertThat(deserializedProtetor.getNomeCompleto()).isEqualTo("Maria Santos");
        assertThat(deserializedProtetor.getUserType()).isEqualTo(UserType.PROTETOR);
        assertThat(deserializedProtetor.getCapacidadeAcolhimento()).isEqualTo(5);
        assertThat(deserializedProtetor.getDisplayName()).isEqualTo("Maria Santos");
        assertThat(deserializedProtetor.getIdentifier()).isEqualTo("12345678901");
    }
    
    @Test
    void shouldHandlePolymorphicSerialization() throws Exception {
        BaseUser[] users = {
            User.builder().name("João").email("joao@teste.com").password("123").celular("41999999999").build(),
            ONG.builder().nomeOng("ONG").cnpj("12345678000190").email("ong@teste.com").password("123").celular("41888888888").build(),
            Protetor.builder().nomeCompleto("Maria").cpf("12345678901").email("maria@teste.com").password("123").celular("41777777777").build()
        };
        
        String json = objectMapper.writeValueAsString(users);
        BaseUser[] deserializedUsers = objectMapper.readValue(json, BaseUser[].class);
        
        assertThat(deserializedUsers).hasSize(3);
        assertThat(deserializedUsers[0]).isInstanceOf(User.class);
        assertThat(deserializedUsers[1]).isInstanceOf(ONG.class);
        assertThat(deserializedUsers[2]).isInstanceOf(Protetor.class);
    }
}