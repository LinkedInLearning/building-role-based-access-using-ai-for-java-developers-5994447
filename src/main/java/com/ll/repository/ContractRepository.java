package com.ll.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ll.model.Contract;

public interface ContractRepository extends MongoRepository<Contract, String> {
  
}
