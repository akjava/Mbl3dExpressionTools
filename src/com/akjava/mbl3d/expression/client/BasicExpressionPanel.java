package com.akjava.mbl3d.expression.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.three.client.gwt.JSParameter;
import com.akjava.gwt.three.client.gwt.ui.LabeledInputRangeWidget2;
import com.akjava.gwt.three.client.js.objects.Mesh;
import com.akjava.mbl3d.expression.client.Mbl3dExpression.ClosedResult;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BasicExpressionPanel extends VerticalPanel {

	private Map<String,LabeledInputRangeWidget2> ranges;
	private List<Mbl3dExpression> expressionList;
	private ValueListBox<Mbl3dExpression> expressionsListBox;
	Mbl3dExpressionReceiver receiver;
	private Button overwriteButton;
	
	public BasicExpressionPanel(final Mesh morphMesh,final Mbl3dExpressionReceiver receiver){
		this.receiver=receiver;
		ranges=Maps.newHashMap();
		Label expression=new Label("Expression");
		
		this.add(expression);
		
		expressionsListBox = new ValueListBox<Mbl3dExpression>(new Renderer<Mbl3dExpression>() {
			@Override
			public String render(Mbl3dExpression object) {
				if(object!=null){
					return object.getName();
				}
				return "";
			}

			@Override
			public void render(Mbl3dExpression object, Appendable appendable) throws IOException {
				// TODO Auto-generated method stub
				
			}
		});
		expressionsListBox.addValueChangeHandler(new ValueChangeHandler<Mbl3dExpression>() {
			@Override
			public void onValueChange(ValueChangeEvent<Mbl3dExpression> event) {
				
				setMbl3dExpression(event.getValue());
				setOverwriteEnable(false);
				
			}
		});
		
		//first column
		HorizontalPanel hpanel=new HorizontalPanel();
		hpanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
		this.add(hpanel);
		
		hpanel.add(expressionsListBox);
		
		Button prev=new Button("Prev",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Mbl3dExpression expression= expressionsListBox.getValue();
				int index=expressionList.indexOf(expression);
				index--;
				if(index<0){
					index=expressionList.size()-1;
				}
				expressionsListBox.setValue(expressionList.get(index),true);
			}
		});
		hpanel.add(prev);
		
		Button next=new Button("Next",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Mbl3dExpression expression= expressionsListBox.getValue();
				int index=expressionList.indexOf(expression);
				index++;
				if(index>=expressionList.size()){
					index=0;
				}
				expressionsListBox.setValue(expressionList.get(index),true);
			}
		});
		hpanel.add(next);
		
		Button store=new Button("Store",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Mbl3dExpression expression= new Mbl3dExpression();
				
				for(String key:ranges.keySet()){
					LabeledInputRangeWidget2 widget=ranges.get(key);
					
					//need only
					double value=widget.getValue();
					if(value!=0){
						expression.set(key, value);
					}
					
				}
				receiver.receive(expression,false);
			}
		});
		hpanel.add(store);
		
		overwriteButton = new Button("Over..",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Mbl3dExpression expression= new Mbl3dExpression();
				
				for(String key:ranges.keySet()){
					LabeledInputRangeWidget2 widget=ranges.get(key);
					//need only
					double value=widget.getValue();
					if(value!=0){
						expression.set(key, value);
					}
				}
				receiver.receive(expression,true);
			}
		});
		hpanel.add(overwriteButton);
		overwriteButton.setTitle("Overwrite current selection");
		overwriteButton.setEnabled(false);
		
		
		Label morph=new Label("Morph");
		this.add(morph);
		
		
		
		String debug="";//for get key all
		JSParameter param=morphMesh.getMorphTargetDictionary().cast();
		
		JsArrayString keys=param.getKeys();
		for(int i=0;i<keys.length();i++){
			String key=keys.get(i);
			final int index=param.getInt(key);
			String originKey=key;
			key=key.substring("Expressions_".length());
			HorizontalPanel inputPanel=new HorizontalPanel();
			
			Label nameLabel=new Label(key);
			
			
			inputPanel.add(nameLabel);
			nameLabel.setWidth("85px");
			
			
			
			final LabeledInputRangeWidget2 inputRange=new LabeledInputRangeWidget2(key, 0, 1, 0.01);
			inputRange.getLabel().setVisible(false);
			inputRange.getRange().setWidth("110px");
			
			ranges.put(originKey, inputRange);
			inputRange.getTextBox().setHeight("12px");
			
			
			debug+=key+"\n";
			
			
			inputRange.setValue(0);
			
			Button zeorBt=new Button("0",new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					inputRange.setValue(0, true);
				}
			});
			inputPanel.add(zeorBt);
			Button fullBt=new Button("1",new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					inputRange.setValue(1, true);
				}
			});
			inputPanel.add(fullBt);
			
			inputPanel.add(inputRange);
			
			this.add(inputPanel);
			inputRange.addtRangeListener(new ValueChangeHandler<Number>() {
				@Override
				public void onValueChange(ValueChangeEvent<Number> event) {
					morphMesh.getMorphTargetInfluences().set(index, event.getValue().doubleValue());
				}
			});
		}
	}
	
	public void setOverwriteEnable(boolean enabled){
		overwriteButton.setEnabled(enabled);
	}
	
	public void setMbl3dExpression(@Nullable Mbl3dExpression expression) {
		//updateClosedLabel(expression);
		
		//TODO not set direct via label
		for(String key:ranges.keySet()){
			LabeledInputRangeWidget2 widget=ranges.get(key);
			widget.setValue(0,true);
		}
			
		//clear 0 first
		/*
		for(int i=0;i<mesh.getMorphTargetInfluences().length();i++){
			mesh.getMorphTargetInfluences().set(i,0);
		}
		*/
		
		if(expression==null){
			//no value reset
			return;
		}
		
		//set new values
		for(String key:expression.getKeys()){
			
			LabeledInputRangeWidget2 widget=ranges.get(key);
			double value=expression.get(key);
			widget.setValue(value,true);
			
			/*
			int index=mesh.getMorphTargetIndexByName(key);
			double value=expression.get(key);
			mesh.getMorphTargetInfluences().set(index, value);
			*/
		}
	}
	public Mbl3dExpression currentRangesToMbl3dExpression() {
		Mbl3dExpression expression=new Mbl3dExpression();
		for(String key:ranges.keySet()){
			LabeledInputRangeWidget2 widget=ranges.get(key);
			if(widget.getValue()!=0){
				expression.set(key, widget.getValue());
			}
		}
		
		return expression;
	}

	public void setExpressionList(List<Mbl3dExpression> expressionList) {
		this.expressionList=expressionList;
		expressionsListBox.setValue(expressionList.get(0));
		expressionsListBox.setAcceptableValues(expressionList);
		
		//testConverter();
	}
	private void testConverter(List<Mbl3dExpression> expressionList){
		Mbl3dExpressionConverter converter=new Mbl3dExpressionConverter();
		Converter<Mbl3dExpression, String> reverse=new Mbl3dExpressionConverter().reverse();
		//test
		for(Mbl3dExpression expression:expressionList){
			if(expression==null){
				continue;
			}
			String json=reverse.convert(expression);
			Mbl3dExpression converted=converter.convert(json);
			LogUtils.log(expression.getName());
			
			new MapDifferenceLogger<String,Double>().compare(expression.getMap(),converted.getMap());
			
		}
	}
	
	public static class MapDifferenceLogger<T,E>{
		public boolean compare(Map<T,E> map1,Map<T,E> map2){
			MapDifference<T, E> difference=Maps.difference(map1,map2);
			boolean result=difference.areEqual();
			if(!result){
				Map<T,ValueDifference<E>> diffs=difference.entriesDiffering();
				for(T key:diffs.keySet()){
					LogUtils.log(key+","+diffs.get(key).leftValue()+","+diffs.get(key).rightValue());
				}
			}
			return result;
		}
	}
	
	public ClosedResult findClosed(Mbl3dExpression expression){
		return Mbl3dExpression.findClosed(expression, expressionList);
	}
	
}
