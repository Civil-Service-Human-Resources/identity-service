package uk.gov.cshr.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface CompoundRoleRepository {

    List<String> getRoles(ICompoundRole role);
    default List<String> getRoles(Collection<ICompoundRole> roles) {
        return roles.stream().flatMap(r -> this.getRoles(r).stream()).collect(Collectors.toList());
    };
}
