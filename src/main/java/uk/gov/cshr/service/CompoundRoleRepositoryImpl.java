package uk.gov.cshr.service;

import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class CompoundRoleRepositoryImpl implements CompoundRoleRepository {
    @Override
    public List<String> getRoles(ICompoundRole role) {
        return role.getRoles();
    }

}
