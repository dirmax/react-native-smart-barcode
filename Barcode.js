/*
 * A smart barcode scanner for react-native apps
 * https://github.com/react-native-component/react-native-smart-barcode/
 * Released under the MIT license
 * Copyright (c) 2016 react-native-component <moonsunfall@aliyun.com>
 */
import React, {
    Component,
} from 'react';
import PropTypes from 'prop-types';
import {
    View,
    requireNativeComponent,
    NativeModules,
    AppState,
    Platform,
	StyleSheet,
	Dimensions,
	Image,
	TouchableOpacity
} from 'react-native'

const BarcodeManager = Platform.OS == 'ios' ? NativeModules.Barcode : NativeModules.CaptureModule

export default class Barcode extends Component {

	constructor(props) {
		super(props);
		this.state = {
			isEnableFlash: false
		};
	}

    static defaultProps = {
        barCodeTypes: Object.values(BarcodeManager.barCodeTypes),
		possiblePointsColor: "#ffbe59",
        scannerRectCornerColor: "#09BB0D",
        scannerRectCornerWidth: 2
    }

    static propTypes = {
        ...View.propTypes,
        onBarCodeRead: PropTypes.func.isRequired,
        barCodeTypes: PropTypes.array,
        possiblePointsColor: PropTypes.string,
        scannerRectCornerColor: PropTypes.string,
        scannerRectCornerWidth: PropTypes.number
    }

    render() {
		
		let {height, width} = Dimensions.get('window'),
			_widthPreview = (width * 3 / 4),
			_borderVertical = (width - _widthPreview) / 2,
			_borderHorizontal = (height - _widthPreview) / 2 - 20
		;
		
		let icoFlash = this.state.isEnableFlash 
			? require('./assets/img/ico_flash_on.png')
			: require('./assets/img/ico_flash_off.png');
		
        return (
			<View style={styles.container} >
	            <NativeBarCode {...this.props} />
				<View style={[styles.overlay, {
					borderLeftWidth: _borderVertical, 
					borderRightWidth: _borderVertical,
					borderTopWidth: _borderHorizontal,
					borderBottomWidth: _borderHorizontal
				}]}>
					<Image 
						source={require('./assets/img/ico_barcode.png')}
						style={[styles.imgBarcode, {width: _widthPreview, height: 30}]}
						resizeMode="contain"
						/>
					<TouchableOpacity onPress={() => {this.toggleFlash()}} style={styles.imgFlash}>
						<Image 
							source={icoFlash}
							resizeMode="contain"
							style={{width: 16, height: 16}}
							/>
					</TouchableOpacity>
					<View style={[
							styles.overlayBorder, 
							styles.overlayLeftTop, 
							{
								borderColor: this.props.scannerRectCornerColor,
								borderLeftWidth: this.props.scannerRectCornerWidth,
								borderTopWidth: this.props.scannerRectCornerWidth,
							}
						]}/>
					<View style={[
							styles.overlayBorder, 
							styles.overlayRightTop,
							{
								borderColor: this.props.scannerRectCornerColor,
								borderRightWidth: this.props.scannerRectCornerWidth,
								borderTopWidth: this.props.scannerRectCornerWidth,
							}
						]}/>
					<View style={[
							styles.overlayBorder, 
							styles.overlayLeftBottom, 
							{
								borderColor: this.props.scannerRectCornerColor,
								borderLeftWidth: this.props.scannerRectCornerWidth,
								borderBottomWidth: this.props.scannerRectCornerWidth,
							}
						]}/>
					<View style={[
							styles.overlayBorder, 
							styles.overlayRightBottom,
							{
								borderColor: this.props.scannerRectCornerColor,
								borderRightWidth: this.props.scannerRectCornerWidth,
								borderBottomWidth: this.props.scannerRectCornerWidth,
							}
						]}/>
				</View>
				
			</View>
        )

    }

    componentDidMount() {
        AppState.addEventListener('change', this._handleAppStateChange);
    }
    componentWillUnmount() {
        AppState.removeEventListener('change', this._handleAppStateChange);
    }

    startScan() {
        BarcodeManager.startSession();
		this._isEnableFlash = false;
    }

    stopScan() {
        BarcodeManager.stopSession()
    }
	
	toggleFlash() {

		(this.state.isEnableFlash == false)
        	? BarcodeManager.startFlash()
        	: BarcodeManager.stopFlash();
		
		this.setState({
			isEnableFlash: !this.state.isEnableFlash
		});
		
    }

    _handleAppStateChange = (currentAppState) => {
        if(currentAppState !== 'active' ) {
            this.stopScan()
        }
        else {
            this.startScan()
        }
    }
}

const styles = StyleSheet.create({
	container: {
		position: 'absolute',
		top: 0,
		left: 0,
		bottom: 0,
		right: 0,
	},
	overlay: {
		position: 'absolute',
		top: 0,
		left: 0,
		bottom: 0,
		right: 0,
    	borderColor: 'rgba(0, 0, 0, 0.5)',
	},
	overlayBorder: {
		position: 'absolute',
		width: 50,
		height: 50
	},
	overlayLeftTop: { top: 0, left: 0 },
	overlayRightTop: { right: 0, top: 0 },
	overlayLeftBottom: { left: 0, bottom: 0 },
	overlayRightBottom: { bottom: 0, right: 0 },
	imgBarcode: {
		position: 'absolute',
		bottom: -50
	},
	imgFlash: {
		position: 'absolute',
		right: -40,
		top: -10,
		padding: 10
	}
});

const NativeBarCode = requireNativeComponent(Platform.OS == 'ios' ? 'RCTBarcode' : 'CaptureView', Barcode);
