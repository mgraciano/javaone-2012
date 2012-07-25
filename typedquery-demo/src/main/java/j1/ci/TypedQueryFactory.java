package j1.ci;

import java.lang.reflect.ParameterizedType;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class TypedQueryFactory {

    @PersistenceContext
    EntityManager em;

    @Produces
    public <T> TypedQuery<T> create(InjectionPoint ip) {
        final ParameterizedType type = (ParameterizedType) ip.getType();
        @SuppressWarnings("unchecked")
        final Class<T> paramType = (Class<T>) type.getActualTypeArguments()[0];

        if (ip.getAnnotated().isAnnotationPresent(QueryName.class)) {
            return em.createNamedQuery(ip.getAnnotated().getAnnotation(
                    QueryName.class).value(), paramType);
        }
        return em.createNamedQuery(queryNameFromField(ip, paramType), paramType);
    }

    private <T> String queryNameFromField(final InjectionPoint ip,
            final Class<T> paramType) {
        final String queryName = ip.getMember().getName();
        return paramType.getSimpleName() + "." + queryName;
    }
}
