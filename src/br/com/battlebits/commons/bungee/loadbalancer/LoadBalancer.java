package br.com.battlebits.commons.bungee.loadbalancer;

import br.com.battlebits.commons.bungee.loadbalancer.element.LoadBalancerObject;

public interface LoadBalancer<T extends LoadBalancerObject> {

	public T next();

}
