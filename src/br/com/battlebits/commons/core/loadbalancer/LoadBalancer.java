package br.com.battlebits.commons.core.loadbalancer;

import br.com.battlebits.commons.core.loadbalancer.element.LoadBalancerObject;

public interface LoadBalancer<T extends LoadBalancerObject> {

	public T next();

}
