package br.edu.utfpr.alunos.webpet.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import br.edu.utfpr.alunos.webpet.domain.pet.Especie;
import br.edu.utfpr.alunos.webpet.domain.pet.Genero;
import br.edu.utfpr.alunos.webpet.domain.pet.Pet;
import br.edu.utfpr.alunos.webpet.domain.pet.Porte;
import br.edu.utfpr.alunos.webpet.domain.user.ONG;
import br.edu.utfpr.alunos.webpet.dto.pet.PetFilterDTO;
import br.edu.utfpr.alunos.webpet.repositories.ONGRepository;
import br.edu.utfpr.alunos.webpet.repositories.PetRepository;
import br.edu.utfpr.alunos.webpet.services.pet.PetService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PetSearchPerformanceTest {

    @Autowired
    private PetRepository petRepository;
    
    @Autowired
    private ONGRepository ongRepository;
    
    @Autowired
    private PetService petService;
    
    private ONG testONG;
    private static final int TOTAL_PETS = 1000;
    
    @BeforeEach
    void setUp() {
        // Create test ONG
        testONG = ONG.builder()
            .cnpj("12.345.678/0001-90")
            .nomeOng("Performance Test ONG")
            .email("performance@test.com")
            .password("password")
            .celular("11999999999")
            .build();
        testONG = ongRepository.save(testONG);
        
        // Create test pets for performance testing
        List<Pet> pets = new ArrayList<>();
        String[] racas = {"Labrador", "Golden Retriever", "Poodle", "Bulldog", "Beagle"};
        Especie[] especies = Especie.values();
        Genero[] generos = Genero.values();
        Porte[] portes = Porte.values();
        
        for (int i = 0; i < TOTAL_PETS; i++) {
            Pet pet = Pet.builder()
                .nome("Pet " + i)
                .especie(especies[i % especies.length])
                .raca(racas[i % racas.length])
                .genero(generos[i % generos.length])
                .porte(portes[i % portes.length])
                .dataNascimento(LocalDate.now().minusYears(i % 10 + 1))
                .descricao("Descrição do pet " + i)
                .responsavel(testONG)
                .build();
            pets.add(pet);
        }
        
        petRepository.saveAll(pets);
    }
    
    @Test
    void shouldPerformFastUnfilteredSearch() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        StopWatch stopWatch = new StopWatch();
        
        // When
        stopWatch.start();
        Page<Pet> result = petRepository.findAllAvailablePets(pageable);
        stopWatch.stop();
        
        // Then
        assertThat(result.getContent()).hasSize(20);
        assertThat(result.getTotalElements()).isGreaterThan(0);
        
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("Unfiltered search execution time: " + executionTime + "ms");
        
        // Performance assertion: should complete within 100ms
        assertThat(executionTime).isLessThan(100);
    }
    
    @Test
    void shouldPerformFastFilteredSearch() {
        // Given
        PetFilterDTO filters = new PetFilterDTO(
            Especie.CACHORRO, Genero.MACHO, Porte.GRANDE, 2, 6, "Labrador"
        );
        Pageable pageable = PageRequest.of(0, 20);
        StopWatch stopWatch = new StopWatch();
        
        // When
        stopWatch.start();
        Page<Pet> result = petRepository.findAvailablePetsWithFilters(
            filters.especie(), filters.genero(), filters.porte(),
            filters.idadeMinima(), filters.idadeMaxima(), filters.raca(),
            pageable
        );
        stopWatch.stop();
        
        // Then
        assertThat(result.getContent()).isNotEmpty();
        
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("Filtered search execution time: " + executionTime + "ms");
        
        // Performance assertion: should complete within 150ms
        assertThat(executionTime).isLessThan(150);
    }
    
    @Test
    void shouldPerformFastTextSearch() {
        // Given
        String searchTerm = "Pet";
        Pageable pageable = PageRequest.of(0, 20);
        StopWatch stopWatch = new StopWatch();
        
        // When
        stopWatch.start();
        Page<Pet> result = petRepository.searchPets(searchTerm, pageable);
        stopWatch.stop();
        
        // Then
        assertThat(result.getContent()).isNotEmpty();
        
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("Text search execution time: " + executionTime + "ms");
        
        // Performance assertion: should complete within 200ms
        assertThat(executionTime).isLessThan(200);
    }
    
    @Test
    void shouldPerformFastCountQuery() {
        // Given
        PetFilterDTO filters = new PetFilterDTO(
            Especie.CACHORRO, null, null, null, null, null
        );
        StopWatch stopWatch = new StopWatch();
        
        // When
        stopWatch.start();
        Long count = petRepository.countAvailablePetsWithFilters(
            filters.especie(), filters.genero(), filters.porte(),
            filters.idadeMinima(), filters.idadeMaxima(), filters.raca()
        );
        stopWatch.stop();
        
        // Then
        assertThat(count).isGreaterThan(0);
        
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("Count query execution time: " + executionTime + "ms");
        
        // Performance assertion: should complete within 50ms
        assertThat(executionTime).isLessThan(50);
    }
    
    @Test
    void shouldHandleMultipleConcurrentSearches() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int searchesPerThread = 5;
        List<Thread> threads = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < searchesPerThread; j++) {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    
                    petRepository.findAllAvailablePets(PageRequest.of(0, 10));
                    
                    stopWatch.stop();
                    synchronized (executionTimes) {
                        executionTimes.add(stopWatch.getTotalTimeMillis());
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(TimeUnit.SECONDS.toMillis(30)); // 30 second timeout
        }
        
        // Then
        assertThat(executionTimes).hasSize(numberOfThreads * searchesPerThread);
        
        double averageTime = executionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        System.out.println("Average concurrent search time: " + averageTime + "ms");
        System.out.println("Max concurrent search time: " + 
            executionTimes.stream().mapToLong(Long::longValue).max().orElse(0) + "ms");
        
        // Performance assertion: average should be under 300ms even with concurrency
        assertThat(averageTime).isLessThan(300);
    }
    
    @Test
    void shouldMaintainPerformanceWithLargePagination() {
        // Given
        Pageable pageable = PageRequest.of(10, 50); // Page 10 with 50 items per page
        StopWatch stopWatch = new StopWatch();
        
        // When
        stopWatch.start();
        Page<Pet> result = petRepository.findAllAvailablePets(pageable);
        stopWatch.stop();
        
        // Then
        long executionTime = stopWatch.getTotalTimeMillis();
        System.out.println("Large pagination execution time: " + executionTime + "ms");
        
        // Performance assertion: should maintain performance even with large offsets
        assertThat(executionTime).isLessThan(200);
    }
}