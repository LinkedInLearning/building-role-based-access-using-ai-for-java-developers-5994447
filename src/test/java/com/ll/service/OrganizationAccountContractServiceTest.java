package com.ll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ll.model.Contract;
import com.ll.model.OrganizationAccount;
import com.ll.model.OrganizationRole;
import com.ll.model.PersonalAccount;
import com.ll.repository.AccountRepository;
import com.ll.repository.ContractRepository;

@SpringBootTest
public class OrganizationAccountContractServiceTest {

        @Autowired
        private PersonalAccountService personalAccountService;

        @Autowired
        private OrganizationAccountService organizationAccountService;

        @Autowired
        private OrganizationContractService contractService;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private ContractRepository contractRepository;

        @BeforeEach
        void cleanup() {
                contractRepository.deleteAll();
                accountRepository.deleteAll();
        }

        /**
         * 1. Create personal accounts for Alice
         * 2. Create an organization account for Tech Corp with Alice as the owner
         * 3. Create a contract for Tech Corp
         * 4. Verify the contract is created
         * 5. Update the contract
         * 6. Verify the contract is updated
         * 7. Delete the contract
         * 8. Verify the contract is deleted
         */
        @Test
        void testOrganizationOwnerContractFlow() {
            // Create Alice's account
            PersonalAccount alice = personalAccountService.createPersonalAccount("alice@example.com", "password");

            // Create Tech Corp with Alice as owner
            OrganizationAccount techCorp = organizationAccountService.createOrganization(alice, "Tech Corp", "A tech company");

            // Create contract
            Contract contract = contractService.createContract(alice, techCorp, "Software License", "Terms and conditions");
            assertNotNull(contract);
            assertEquals("Software License", contract.getName());

            // Update contract
            contract = contractService.updateContract(alice, techCorp, contract.getId(), "Updated License", "New terms");
            assertEquals("Updated License", contract.getName());
            assertEquals("New terms", contract.getDescription());

            // Delete contract
            contractService.deleteContract(alice, techCorp, contract.getId());
            Optional<Contract> deletedContract = contractService.getContract(alice, techCorp, contract.getId());
            assertFalse(deletedContract.isPresent());
        }

        /**
         * 1. Create personal accounts for Alice, Bob
         * 2. Create an organization account for Tech Corp with Alice as the owner
         * 3. Add Bob as Editor
         * 4. Create a contract for Tech Corp
         * 5. Verify Bob can read the contract
         * 6. Verify Bob can update the contract
         * 7. Verify Bob cannot delete the contract
         */
        @Test
        void testOrganizationEditorContractFlow() {
            // Create accounts
            PersonalAccount alice = personalAccountService.createPersonalAccount("alice@example.com", "password");
            PersonalAccount bob = personalAccountService.createPersonalAccount("bob@example.com", "password");

            // Create Tech Corp with Alice as owner
            OrganizationAccount techCorp = organizationAccountService.createOrganization(alice, "Tech Corp", "A tech company");
            
            // Add Bob as Editor
            OrganizationAccount updatedOrg = organizationAccountService.addMember(alice, techCorp.getId(), bob.getId(), OrganizationRole.EDITOR);

            // Create contract as Alice
            Contract contract = contractService.createContract(alice, updatedOrg, "Software License", "Terms and conditions");
            
            // Verify Bob can read
            Optional<Contract> bobsView = contractService.getContract(bob, updatedOrg, contract.getId());
            assertTrue(bobsView.isPresent());
            
            // Verify Bob can update
            Contract updatedContract = contractService.updateContract(bob, updatedOrg, contract.getId(), "Updated by Bob", "New terms");
            assertEquals("Updated by Bob", updatedContract.getName());
            
            // Verify Bob cannot delete
            assertThrows(IllegalStateException.class, () -> 
                contractService.deleteContract(bob, updatedOrg, contract.getId())
            );
        }

        /**
         * 1. Create personal accounts for Alice, Charlie
         * 2. Create an organization account for Tech Corp with Alice as the owner
         * 3. Add Charlie as Viewer
         * 4. Create a contract for Tech Corp
         * 5. Verify Charlie can read the contract
         * 6. Verify Charlie cannot update the contract
         * 7. Verify Charlie cannot delete the contract
         */
        @Test
        void testOrganizationViewerContractFlow() {
            // Create accounts
            PersonalAccount alice = personalAccountService.createPersonalAccount("alice@example.com", "password");
            PersonalAccount charlie = personalAccountService.createPersonalAccount("charlie@example.com", "password");

            // Create Tech Corp with Alice as owner
            OrganizationAccount techCorp = organizationAccountService.createOrganization(alice, "Tech Corp", "A tech company");
            
            // Add Charlie as Viewer
            OrganizationAccount updatedOrg = organizationAccountService.addMember(alice, techCorp.getId(), charlie.getId(), OrganizationRole.VIEWER);

            // Create contract as Alice
            Contract contract = contractService.createContract(alice, updatedOrg, "Software License", "Terms and conditions");
            
            // Verify Charlie can read
            Optional<Contract> charliesView = contractService.getContract(charlie, updatedOrg, contract.getId());
            assertTrue(charliesView.isPresent());
            
            // Verify Charlie cannot update
            assertThrows(IllegalStateException.class, () -> 
                contractService.updateContract(charlie, updatedOrg, contract.getId(), "Updated by Charlie", "New terms")
            );
            
            // Verify Charlie cannot delete
            assertThrows(IllegalStateException.class, () -> 
                contractService.deleteContract(charlie, updatedOrg, contract.getId())
            );
        }

}
