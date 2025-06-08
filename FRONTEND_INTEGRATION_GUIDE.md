# 🔗 Frontend Integration Guide - WebPet API

## 📋 Índice
1. [Quick Start](#quick-start)
2. [Configuração Inicial](#configuração-inicial)
3. [Autenticação](#autenticação)
4. [Exemplos de Integração](#exemplos-de-integração)
5. [Error Handling](#error-handling)
6. [Upload de Arquivos](#upload-de-arquivos)
7. [Testes da API](#testes-da-api)

---

## 🚀 Quick Start

### 📡 **Base URL**
```
http://localhost:8081/api
```

### 🔧 **Headers Obrigatórios**
```javascript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer <seu-jwt-token>' // Para endpoints autenticados
}
```

### 🎯 **CORS Configurado Para:**
- `http://localhost:3000` (React dev server)
- `http://localhost:8080` (Vue dev server)
- `http://127.0.0.1:3000`

---

## ⚙️ Configuração Inicial

### 🌐 **Axios Setup (React/Vue)**

```javascript
// api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Interceptor para incluir token automaticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para tratar refresh de token
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expirado, tentar refresh
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post('/auth/refresh', {
          refreshToken
        });
        
        localStorage.setItem('accessToken', response.data.accessToken);
        
        // Repetir requisição original
        error.config.headers.Authorization = `Bearer ${response.data.accessToken}`;
        return api.request(error.config);
      } catch (refreshError) {
        // Refresh falhou, redirecionar para login
        localStorage.clear();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 🔄 **Fetch API Setup (Vanilla JS)**

```javascript
// apiClient.js
class ApiClient {
  constructor() {
    this.baseURL = 'http://localhost:8081/api';
    this.timeout = 10000;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const token = localStorage.getItem('accessToken');
    
    const config = {
      timeout: this.timeout,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, config);
      
      if (response.status === 401) {
        await this.refreshToken();
        // Retry request
        config.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`;
        return fetch(url, config);
      }

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  async refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await fetch(`${this.baseURL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
      localStorage.clear();
      window.location.href = '/login';
      throw new Error('Token refresh failed');
    }

    const data = await response.json();
    localStorage.setItem('accessToken', data.accessToken);
  }
}

export default new ApiClient();
```

---

## 🔐 Autenticação

### 📝 **1. Registro de Usuário**

```javascript
// userService.js
export const registerUser = async (userData) => {
  try {
    const response = await api.post('/auth/register', {
      name: userData.name,
      surname: userData.surname,
      email: userData.email,
      password: userData.password,
      celular: userData.phone
    });
    
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Exemplo de uso no componente
const handleRegister = async (formData) => {
  try {
    const result = await registerUser(formData);
    console.log('Usuário criado:', result);
    // Redirecionar para confirmação de email
    navigate('/email-confirmation');
  } catch (error) {
    setError(error.message || 'Erro ao criar usuário');
  }
};
```

### 🔑 **2. Login**

```javascript
export const login = async (email, password) => {
  try {
    const response = await api.post('/auth/login', {
      email,
      password
    });

    const { accessToken, refreshToken, user } = response.data;
    
    // Salvar tokens
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));
    
    return response.data;
  } catch (error) {
    throw error.response?.data || error.message;
  }
};

// Hook React para autenticação
import { useState, useContext, createContext } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  const loginUser = async (email, password) => {
    const data = await login(email, password);
    setUser(data.user);
    return data;
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loginUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

### 🏢 **3. Registro de ONG/Protetor**

```javascript
export const registerONG = async (ongData) => {
  return api.post('/auth/register/ong', {
    nomeOng: ongData.name,
    cnpj: ongData.cnpj,
    email: ongData.email,
    password: ongData.password,
    celular: ongData.phone,
    endereco: ongData.address,
    descricao: ongData.description
  });
};

export const registerProtetor = async (protetorData) => {
  return api.post('/auth/register/protetor', {
    nomeCompleto: protetorData.fullName,
    cpf: protetorData.cpf,
    email: protetorData.email,
    password: protetorData.password,
    celular: protetorData.phone,
    endereco: protetorData.address,
    capacidadeAcolhimento: protetorData.capacity
  });
};
```

---

## 🔌 Exemplos de Integração

### 🐕 **1. Listagem de Pets**

```javascript
// petService.js
export const getPets = async (filters = {}) => {
  try {
    const params = new URLSearchParams();
    
    if (filters.especie) params.append('especie', filters.especie);
    if (filters.porte) params.append('porte', filters.porte);
    if (filters.cidade) params.append('cidade', filters.cidade);
    if (filters.page) params.append('page', filters.page);
    if (filters.size) params.append('size', filters.size);

    const response = await api.get(`/pets?${params}`);
    return response.data;
  } catch (error) {
    console.error('Erro ao buscar pets:', error);
    throw error;
  }
};

// Componente React
import { useState, useEffect } from 'react';

const PetList = () => {
  const [pets, setPets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    especie: '',
    porte: '',
    page: 0,
    size: 12
  });

  useEffect(() => {
    const fetchPets = async () => {
      try {
        setLoading(true);
        const data = await getPets(filters);
        setPets(data.content);
      } catch (error) {
        console.error('Erro:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPets();
  }, [filters]);

  const handleFilterChange = (newFilters) => {
    setFilters(prev => ({ ...prev, ...newFilters, page: 0 }));
  };

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <PetFilters onFilterChange={handleFilterChange} />
      <div className="pet-grid">
        {pets.map(pet => (
          <PetCard key={pet.id} pet={pet} />
        ))}
      </div>
    </div>
  );
};
```

### 📝 **2. Cadastro de Pet**

```javascript
export const createPet = async (petData) => {
  return api.post('/pets', {
    nome: petData.name,
    especie: petData.species,
    raca: petData.breed,
    genero: petData.gender,
    porte: petData.size,
    dataNascimento: petData.birthDate,
    descricao: petData.description,
    fotoUrl: petData.photoUrl
  });
};

// Formulário de cadastro
const PetForm = () => {
  const [formData, setFormData] = useState({
    name: '',
    species: '',
    breed: '',
    gender: '',
    size: '',
    birthDate: '',
    description: '',
    photoUrl: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createPet(formData);
      alert('Pet cadastrado com sucesso!');
      // Reset form or redirect
    } catch (error) {
      alert('Erro ao cadastrar pet: ' + error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Nome do pet"
        value={formData.name}
        onChange={(e) => setFormData({...formData, name: e.target.value})}
        required
      />
      
      <select
        value={formData.species}
        onChange={(e) => setFormData({...formData, species: e.target.value})}
        required
      >
        <option value="">Selecione a espécie</option>
        <option value="CACHORRO">Cachorro</option>
        <option value="GATO">Gato</option>
        <option value="COELHO">Coelho</option>
      </select>

      {/* Outros campos... */}
      
      <button type="submit">Cadastrar Pet</button>
    </form>
  );
};
```

### 💰 **3. Sistema de Doações**

```javascript
// donationService.js
export const makeDonation = async (donationData) => {
  return api.post('/donations', {
    nomeDoador: donationData.donorName,
    emailDoador: donationData.donorEmail,
    telefoneDoador: donationData.donorPhone,
    valor: donationData.amount,
    tipoDoacao: donationData.type,
    mensagem: donationData.message,
    beneficiarioId: donationData.beneficiaryId
  });
};

export const getMyDonations = async () => {
  return api.get('/donations/my-donations');
};

// Componente de doação
const DonationForm = ({ beneficiaryId, beneficiaryName }) => {
  const [donation, setDonation] = useState({
    donorName: '',
    donorEmail: '',
    donorPhone: '',
    amount: '',
    type: 'MONETARIA',
    message: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await makeDonation({
        ...donation,
        beneficiaryId
      });
      alert('Doação realizada com sucesso!');
    } catch (error) {
      alert('Erro ao processar doação: ' + error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>Fazer doação para {beneficiaryName}</h3>
      
      <input
        type="text"
        placeholder="Seu nome"
        value={donation.donorName}
        onChange={(e) => setDonation({...donation, donorName: e.target.value})}
        required
      />

      <select
        value={donation.type}
        onChange={(e) => setDonation({...donation, type: e.target.value})}
      >
        <option value="MONETARIA">Monetária</option>
        <option value="RACAO">Ração</option>
        <option value="MEDICAMENTOS">Medicamentos</option>
        <option value="BRINQUEDOS">Brinquedos</option>
      </select>

      {donation.type === 'MONETARIA' && (
        <input
          type="number"
          step="0.01"
          placeholder="Valor (R$)"
          value={donation.amount}
          onChange={(e) => setDonation({...donation, amount: e.target.value})}
          required
        />
      )}

      <textarea
        placeholder="Mensagem (opcional)"
        value={donation.message}
        onChange={(e) => setDonation({...donation, message: e.target.value})}
      />

      <button type="submit">Fazer Doação</button>
    </form>
  );
};
```

---

## ❌ Error Handling

### 🚨 **Padrão de Resposta de Erro**

```javascript
// Estrutura padrão de erro da API
{
  "timestamp": "2025-01-08T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/pets",
  "details": {
    "field": "nome",
    "rejectedValue": "",
    "message": "Nome é obrigatório"
  }
}
```

### 🛡️ **Error Handler Global**

```javascript
// errorHandler.js
export const handleApiError = (error) => {
  if (error.response) {
    // Erro da API
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return {
          type: 'validation',
          message: data.message || 'Dados inválidos',
          details: data.details
        };
      
      case 401:
        return {
          type: 'auth',
          message: 'Não autorizado. Faça login novamente.'
        };
      
      case 403:
        return {
          type: 'forbidden',
          message: 'Você não tem permissão para esta ação'
        };
      
      case 404:
        return {
          type: 'notFound',
          message: 'Recurso não encontrado'
        };
      
      case 429:
        return {
          type: 'rateLimit',
          message: 'Muitas tentativas. Tente novamente em alguns minutos.'
        };
      
      case 500:
        return {
          type: 'server',
          message: 'Erro interno do servidor. Tente novamente.'
        };
      
      default:
        return {
          type: 'unknown',
          message: data.message || 'Erro desconhecido'
        };
    }
  } else if (error.request) {
    // Erro de rede
    return {
      type: 'network',
      message: 'Erro de conexão. Verifique sua internet.'
    };
  } else {
    // Erro de configuração
    return {
      type: 'config',
      message: 'Erro na configuração da requisição'
    };
  }
};

// Hook React para tratamento de erros
export const useErrorHandler = () => {
  const [error, setError] = useState(null);

  const handleError = (error) => {
    const processedError = handleApiError(error);
    setError(processedError);
    
    // Log para debug
    console.error('API Error:', error);
    
    // Auto-clear após 5 segundos
    setTimeout(() => setError(null), 5000);
  };

  const clearError = () => setError(null);

  return { error, handleError, clearError };
};
```

### ⚠️ **Componente de Notificação**

```javascript
// ErrorNotification.jsx
const ErrorNotification = ({ error, onClose }) => {
  if (!error) return null;

  const getErrorIcon = (type) => {
    switch (type) {
      case 'validation': return '⚠️';
      case 'auth': return '🔒';
      case 'network': return '🌐';
      case 'server': return '🚫';
      default: return '❌';
    }
  };

  return (
    <div className={`error-notification error-${error.type}`}>
      <span className="error-icon">{getErrorIcon(error.type)}</span>
      <span className="error-message">{error.message}</span>
      <button onClick={onClose} className="error-close">×</button>
    </div>
  );
};
```

---

## 📤 Upload de Arquivos

### 🖼️ **Upload de Fotos de Pets**

```javascript
// uploadService.js
export const uploadPetPhoto = async (file, petId) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('petId', petId);

  return api.post('/pets/upload-photo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      const percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      );
      console.log(`Upload progress: ${percentCompleted}%`);
    }
  });
};

// Componente de upload
const PhotoUpload = ({ petId, onUploadComplete }) => {
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validações
    if (file.size > 10 * 1024 * 1024) { // 10MB
      alert('Arquivo muito grande. Máximo 10MB.');
      return;
    }

    if (!file.type.startsWith('image/')) {
      alert('Apenas imagens são permitidas.');
      return;
    }

    try {
      setUploading(true);
      const response = await uploadPetPhoto(file, petId);
      onUploadComplete(response.data.photoUrl);
      alert('Foto carregada com sucesso!');
    } catch (error) {
      alert('Erro ao carregar foto: ' + error.message);
    } finally {
      setUploading(false);
      setProgress(0);
    }
  };

  return (
    <div className="photo-upload">
      <input
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        disabled={uploading}
      />
      
      {uploading && (
        <div className="upload-progress">
          <div 
            className="progress-bar" 
            style={{ width: `${progress}%` }}
          />
          <span>{progress}%</span>
        </div>
      )}
    </div>
  );
};
```

---

## 🧪 Testes da API

### 🔍 **Testando Endpoints**

```javascript
// api.test.js
import { describe, it, expect, beforeAll } from 'vitest';
import api from './api.js';

describe('WebPet API Tests', () => {
  let authToken;
  let testPetId;

  beforeAll(async () => {
    // Setup: fazer login para obter token
    const loginResponse = await api.post('/auth/login', {
      email: 'test@example.com',
      password: 'Test123!'
    });
    authToken = loginResponse.data.accessToken;
    api.defaults.headers.Authorization = `Bearer ${authToken}`;
  });

  describe('Authentication', () => {
    it('should register new user', async () => {
      const userData = {
        name: 'Test User',
        surname: 'Silva',
        email: 'newuser@example.com',
        password: 'NewUser123!',
        celular: '(11) 99999-9999'
      };

      const response = await api.post('/auth/register', userData);
      expect(response.status).toBe(201);
      expect(response.data.user.email).toBe(userData.email);
    });

    it('should login successfully', async () => {
      const response = await api.post('/auth/login', {
        email: 'test@example.com',
        password: 'Test123!'
      });

      expect(response.status).toBe(200);
      expect(response.data).toHaveProperty('accessToken');
      expect(response.data).toHaveProperty('user');
    });
  });

  describe('Pets', () => {
    it('should get pets list', async () => {
      const response = await api.get('/pets');
      expect(response.status).toBe(200);
      expect(Array.isArray(response.data.content)).toBe(true);
    });

    it('should create new pet', async () => {
      const petData = {
        nome: 'Buddy Test',
        especie: 'CACHORRO',
        raca: 'Labrador',
        genero: 'MACHO',
        porte: 'GRANDE',
        dataNascimento: '2020-01-01',
        descricao: 'Pet para teste'
      };

      const response = await api.post('/pets', petData);
      expect(response.status).toBe(201);
      expect(response.data.nome).toBe(petData.nome);
      testPetId = response.data.id;
    });

    it('should get pet by id', async () => {
      const response = await api.get(`/pets/${testPetId}`);
      expect(response.status).toBe(200);
      expect(response.data.id).toBe(testPetId);
    });
  });

  describe('Donations', () => {
    it('should create donation', async () => {
      const donationData = {
        nomeDoador: 'Test Donor',
        emailDoador: 'donor@example.com',
        valor: 100.00,
        tipoDoacao: 'MONETARIA',
        beneficiarioId: 'some-ong-id'
      };

      const response = await api.post('/donations', donationData);
      expect(response.status).toBe(201);
      expect(response.data.valor).toBe(donationData.valor);
    });
  });
});
```

### 📋 **Checklist de Integração**

```javascript
// integrationChecklist.js
export const runIntegrationChecklist = async () => {
  const results = {
    apiConnection: false,
    authentication: false,
    petsCRUD: false,
    donations: false,
    fileUpload: false,
    errorHandling: false
  };

  try {
    // 1. Teste de conexão
    const healthCheck = await api.get('/actuator/health');
    results.apiConnection = healthCheck.status === 200;

    // 2. Teste de autenticação
    const loginTest = await api.post('/auth/login', {
      email: 'test@example.com',
      password: 'test123'
    });
    results.authentication = loginTest.status === 200;

    // 3. Teste CRUD de pets
    const petsTest = await api.get('/pets');
    results.petsCRUD = petsTest.status === 200;

    // 4. Teste de doações
    const donationsTest = await api.get('/donations/my-donations');
    results.donations = donationsTest.status === 200;

    // 5. Teste de error handling
    try {
      await api.get('/invalid-endpoint');
    } catch (error) {
      results.errorHandling = error.response?.status === 404;
    }

  } catch (error) {
    console.error('Integration test failed:', error);
  }

  return results;
};

// Uso em desenvolvimento
runIntegrationChecklist().then(results => {
  console.table(results);
  const allPassed = Object.values(results).every(Boolean);
  console.log(`Integration Status: ${allPassed ? '✅ PASSED' : '❌ FAILED'}`);
});
```

---

## 📚 Recursos Adicionais

### 🔗 **Links Úteis**
- **Swagger UI**: `http://localhost:8081/swagger-ui/index.html`
- **H2 Console**: `http://localhost:8081/h2-console`
- **Health Check**: `http://localhost:8081/actuator/health`

### 🎨 **Exemplos de UI**

#### **Cards de Pet**
```css
.pet-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  margin: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.pet-photo {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.pet-info h3 {
  margin: 8px 0;
  color: #333;
}

.pet-badges {
  display: flex;
  gap: 4px;
  margin: 8px 0;
}

.badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background: #f0f0f0;
}
```

### 🔧 **Debug e Desenvolvimento**

```javascript
// debug.js
export const debugAPI = {
  logRequest: (config) => {
    console.group('🚀 API Request');
    console.log('URL:', config.url);
    console.log('Method:', config.method);
    console.log('Headers:', config.headers);
    console.log('Data:', config.data);
    console.groupEnd();
  },

  logResponse: (response) => {
    console.group('✅ API Response');
    console.log('Status:', response.status);
    console.log('Data:', response.data);
    console.groupEnd();
  },

  logError: (error) => {
    console.group('❌ API Error');
    console.log('Message:', error.message);
    console.log('Status:', error.response?.status);
    console.log('Data:', error.response?.data);
    console.groupEnd();
  }
};

// Adicionar aos interceptors para debug
api.interceptors.request.use(
  (config) => {
    if (process.env.NODE_ENV === 'development') {
      debugAPI.logRequest(config);
    }
    return config;
  }
);
```

---

**🎯 Com este guia, seu frontend estará pronto para se integrar perfeitamente com a API WebPet!**

*Para dúvidas ou problemas, consulte a documentação completa ou abra uma issue no projeto.*