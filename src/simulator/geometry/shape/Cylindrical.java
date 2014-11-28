package simulator.geometry.shape;

import java.io.Serializable;

import simulator.SpatialGrid;
import simulator.geometry.ContinuousVector;
import simulator.geometry.DiscreteVector;
import simulator.geometry.Domain;
import utils.ExtraMath;
import utils.XMLParser;

public class Cylindrical implements IsShape, Serializable
{
	/**
	 * Serial version used for the serialisation of the class.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * A point on the cylinder axis
	 */
	private DiscreteVector _dPointCenterBase;
	
	/**
	 * 
	 */
	private DiscreteVector _dVectorAlongAxis;
	
	/**
	 * A vector, orthogonal to the vector along the axis, used as a reference
	 * for the angle theta.
	 */
	private DiscreteVector _dVectorRadiusV;
	
	/**
	 * Another vector, orthogonal to the vector along the axis, used for
	 * converting back to Cartesian coordinates.
	 */
	private DiscreteVector _dVectorRadiusW;
	
	private ContinuousVector _cPointCenterBase;
	
	
	private ContinuousVector _cVectorAlongAxis;
	
	/**
	 * A vector, orthogonal to the vector along the axis, used as a reference
	 * for the angle theta.
	 */
	private ContinuousVector _cVectorRadiusV;
	
	/**
	 * Another vector, orthogonal to the vector along the axis, used for
	 * converting back to Cartesian coordinates.
	 */
	private ContinuousVector _cVectorRadiusW;
	
	/**
	 * The radius of this cylinder. Equivalent to _cVectorRadius.norm().
	 */
	private Double _radius;
	
	/**
	 * The length of this cylinder. Equivalent to _cVectorAlongAxis.norm().
	 */
	private Double _length;
	
	/**
	 * Whether the inside of the cylinder is the inside (true) or the outside
	 * (false) of the domain. 
	 */
	private Boolean _interiorMatchesDomain;
	
	
	
	/**
	 * 
	 */
	public void readShape(XMLParser shapeRoot, Domain aDomain)
	{
		_dPointCenterBase = new DiscreteVector(shapeRoot.getParamParser("pointCenter"));
		_dVectorAlongAxis = new DiscreteVector(shapeRoot.getParamParser("vectorAxis"));
		_radius = shapeRoot.getParamLength("radius");
		
		_dVectorRadiusV = new DiscreteVector();
		_dVectorRadiusW = new DiscreteVector();
		_dVectorAlongAxis.orthoVector(_dVectorRadiusV, _dVectorRadiusW);
		
		Double res = aDomain.getResolution();
		_cPointCenterBase = new ContinuousVector(_dPointCenterBase, res);
		_cVectorAlongAxis = new ContinuousVector(_dVectorAlongAxis, res);
		_length = _cVectorAlongAxis.norm();
		_cVectorRadiusV = new ContinuousVector(_dVectorRadiusV, res);
		_cVectorRadiusV.normalizeVector(_radius);
		_cVectorRadiusW = new ContinuousVector(_dVectorRadiusW, res);
		_cVectorRadiusV.normalizeVector(_radius);
		
		_interiorMatchesDomain = shapeRoot.getParamBool("interiorMatchesDomain");
	}
	
	/**
	 * 
	 */
	public Boolean isOutside(ContinuousVector point)
	{
		ContinuousVector baseToPoint = baseToPoint(point);
		Double cosAngle = _cVectorAlongAxis.cosAngle(baseToPoint);
		
		if ( cosAngle < 0 )
			return _interiorMatchesDomain;
		
		Double dist = baseToPoint.norm();
		Double height = dist/cosAngle;
		Double radius = ExtraMath.triangleSide(dist, height);
		
		if ( (radius > _radius) || (height > _length) )
			return _interiorMatchesDomain;
		else
			return ! _interiorMatchesDomain;
	}
	
	/**
	 * 
	 */
	public Boolean isOnBoundary(ContinuousVector cV, Double res)
	{
		
		return null;
	}
	
	/**
	 * 
	 */
	public ContinuousVector intersection(ContinuousVector position,
													ContinuousVector vector)
	{
		
		return null;
	}
	
	/**
	 * 
	 */
	public void orthoProj(ContinuousVector ccIn, ContinuousVector ccOut)
	{
		
		
	}
	
	/**
	 * 
	 */
	public ContinuousVector getOrthoProj(ContinuousVector ccIn)
	{
		
		return null;
	}
	
	/**
	 * 
	 */
	public Double getDistance(ContinuousVector cc)
	{
		
		return null;
	}
	
	/**
	 * 
	 * @param point Must be on (round) surface of cylinder!
	 * @return 2 Doubles: (0) height, (1) angle
	 */
	private Double[] convertToCylindrical(ContinuousVector point)
	{
		Double[] out = new Double[2];
		
		ContinuousVector baseToPoint = baseToPoint(point);
		
		Double cosAngle = _cVectorAlongAxis.cosAngle(baseToPoint);
		Double dist = baseToPoint.norm();
		Double height = dist/cosAngle;
		Double radius = ExtraMath.triangleSide(dist, height);
		
		/* Use Pythagoros to find the height of this point, i.e. the length
		 * along the axis that is closest to the point.
		 */
		out[0] = ExtraMath.triangleSide(baseToPoint.norm(), _radius); 
		
		// Now find the position on the axis that is closest to the point.
		ContinuousVector nearestOnAxis = new ContinuousVector(_cVectorAlongAxis);
		nearestOnAxis.times(out[0]/_length);
		baseToPoint.subtract(nearestOnAxis);
		
		out[1] = _cVectorRadiusV.angle(baseToPoint);
		
		return out;
	}
	
	private void convertToCartesian(Double[] heightTheta, ContinuousVector out)
	{
		out.set(_cPointCenterBase);
		ContinuousVector temp = new ContinuousVector(_cVectorAlongAxis);
		temp.times(heightTheta[0]/_length);
		out.add(temp);
		temp.set(_cVectorRadiusV);
		temp.times(Math.cos(heightTheta[1]));
		out.add(temp);
		temp.set(_cVectorRadiusW);
		temp.times(Math.sin(heightTheta[1]));
		out.add(temp);
	}
	
	private ContinuousVector convertToCartesian(Double[] heightTheta)
	{
		ContinuousVector out = new ContinuousVector();
		convertToCartesian(heightTheta, out);
		return out;
	}
	
	/**
	 * 
	 * 
	 * @param point1
	 * @param point2
	 * @return
	 */
	public Double distance(ContinuousVector point1, ContinuousVector point2)
	{
		Double[] p1 = convertToCylindrical(point1);
		Double[] p2 = convertToCylindrical(point2);
		
		Double angle = Math.abs(p1[1] - p2[1]);
		angle = Math.min(angle, 2*Math.PI);
		
		return Math.hypot(p1[0] - p2[0], _radius*(angle));
	}
	
	/**
	 * 
	 * @param point Any point in Cartesian space.
	 * @return	The vector from _cPointCenterBase to this point.
	 */
	private ContinuousVector baseToPoint(ContinuousVector point)
	{
		ContinuousVector out = new ContinuousVector(point);
		out.subtract(_cPointCenterBase);
		return out;
	}
}
